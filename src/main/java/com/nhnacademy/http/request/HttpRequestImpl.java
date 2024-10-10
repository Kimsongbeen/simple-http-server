/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2024. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.http.request;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class HttpRequestImpl implements HttpRequest {

    private final Socket client;

    private final Map<String,Object> headerMap = new HashMap<>();
    private final Map<String,Object> attributeMap = new HashMap<>();
    private final static String KEY_HTTP_METHOD = "HTTP-METHOD";
    private final static String KEY_QUERY_PARAM_MAP = "HTTP-QUERY-PARAM-MAP";
    private final static String KEY_REQUEST_PATH="HTTP-REQUEST-PATH";
    private final static String HEADER_DELIMER=":";

    public HttpRequestImpl(Socket socket) {
        this.client = socket;
        try {
            initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialize() throws IOException {

        StringBuilder result = new StringBuilder();
        do {
            try {
                result.append((char) client.getInputStream().read());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (client.getInputStream().available() > 0);

        log.debug("result:{}", result);

        String lines[] = result.toString().split(System.lineSeparator());
        int count = 0;
        for(int i=0; i< lines.length; i++){
            String line = lines[i];

            if(Objects.isNull(line) || line.trim().equals("")){
                count++;
                continue;
            }

            if(i==0){
                parseHttpRequestInfo(line);
            }else if(getMethod().equals("GET")){
                log.debug("get-line:{}",line);
                parseHeader(line);
            }else{
                log.debug("in-line:{}",line);
                log.debug("in-count:{}",count);
                if(count>0){
                    parsePostBody(line);
                }
            }

        }

    }
    @Override
    public String getMethod() {
        return String.valueOf(headerMap.get(KEY_HTTP_METHOD));
    }
    @Override
    public String getParameter(String name) {
        return String.valueOf(getParameterMap().get(name));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getParameterMap() {
        return (Map<String, String>) headerMap.get(KEY_QUERY_PARAM_MAP);
    }

    @Override
    public String getHeader(String name) {
        return String.valueOf(headerMap.get(name));
    }
    @Override
    public void setAttribute(String name, Object o) {
        attributeMap.put(name,o);
    }
    @Override
    public Object getAttribute(String name) {
        return attributeMap.get(name);
    }
    @Override
    public String getRequestURI() {
        return String.valueOf(headerMap.get(KEY_REQUEST_PATH));
    }

    @SuppressWarnings("unchecked")
    public void parsePostBody(String s){

        try {
            s = URLDecoder.decode(s,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.debug("parse-body:{}",s);

        String params[] = s.split("&");
        for(String param : params){
            String key = param.split("=")[0];
            String value = param.split("=")[1];
            Map<String,String> map = (Map<String, String>) headerMap.get(KEY_QUERY_PARAM_MAP);
            map.put(key,value);
        }
    }

    private void parseHeader(String s){
        String[] hStr = s.split(HEADER_DELIMER);
        String key = hStr[0].trim();
        String value = hStr[1].trim();

        if(Objects.nonNull(key) && key.length()>0) {
            headerMap.put(key, value);
        }
    }

    private void parseHttpRequestInfo(String s) {
        String arr[] = s.split(" ");
        //http method parse
        if (arr.length > 0) {
            headerMap.put(KEY_HTTP_METHOD, s.split(" ")[0]);
        }
        //query parameter parse
        if (arr.length > 2) {
            Map<String, String> queryMap = new HashMap<>();
            int questionIndex = arr[1].indexOf("?");
            String httpRequestPath;

            if(questionIndex>0){
                httpRequestPath = arr[1].substring(0, questionIndex);
            }else{
                httpRequestPath = arr[1];
            }

            String queryString = arr[1].substring(questionIndex + 1, arr[1].length());

            if (Objects.nonNull(queryString) && !httpRequestPath.equals(queryString) ) {
                String qarr[] = queryString.split("&");
                for (String q : qarr) {
                    String key = q.split("=")[0];
                    String value = q.split("=")[1];
                    log.debug("key:{},value={}", key, value);
                    queryMap.put(key.trim(), value.trim());
                }
            }

            //path 설정
            headerMap.put(KEY_REQUEST_PATH, httpRequestPath);

            //queryMap 설정
            headerMap.put(KEY_QUERY_PARAM_MAP, queryMap);
        }
    }

}
