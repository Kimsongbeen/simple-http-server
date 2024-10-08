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

package com.nhnacademy.http.channel;

import com.nhnacademy.http.request.HttpRequest;
import com.nhnacademy.http.request.HttpRequestImpl;
import com.nhnacademy.http.response.HttpResponse;
import com.nhnacademy.http.response.HttpResponseImpl;
import com.nhnacademy.http.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

@Slf4j
public class HttpJob implements Executable {

    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;

    private final Socket client;

    public HttpJob(Socket client) {
        this.httpRequest = new HttpRequestImpl(client);
        this.httpResponse = new HttpResponseImpl(client);
        this.client = client;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    @Override
    public void execute(){

        String responseBody = null;
        String responseHeader = null;

        log.debug("method:{}", httpRequest.getMethod());
        log.debug("uri:{}", httpRequest.getRequestURI());
        log.debug("clinet-closed:{}",client.isClosed());

        /* #5 Browser(client)는 특정 page를 요청시 먼저 /favicon.ico 호출 합니다.
          아래 코드에서는 /favicon.ico 요청을 처리하지 않고 return 합니다.
        */
        if(httpRequest.getRequestURI().equals("/favicon.ico")){
            return;
        }

        /* #6 /index.html을 요청시  httpRequest.getRequestURI()에 해당되는 html 파일이 존재 하지 않는다면 client 연결을 종료 합니다.
            ex) /index.html 요청이 온다면 ->  /resources/index.html이 존재하지 않는다면 client 연결을 종료 합니다.
            ResponseUtils.isExist(httpRequest.getRequestURI()) 이용하여 구현합니다.
        */
        if(!ResponseUtils.isExist(httpRequest.getRequestURI())){
            try {
                responseBody = ResponseUtils.tryGetBodyFromFile(ResponseUtils.DEFAULT_404);
                responseHeader = ResponseUtils.createResponseHeader(404, "utf-8", responseBody.getBytes("utf-8").length);
            } catch (IOException e) {
                log.debug("file is not exist!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                e.printStackTrace();
            }
        }else{
            // file exist

            /* #8 responseBody에 응답할 html 파일을 읽습니다.
           ResponseUtils.tryGetBodyFromFile(httpRequest.getRequestURI()) 이용하여 구현 합니다.
            */
            try {
                responseBody = ResponseUtils.tryGetBodyFromFile(httpRequest.getRequestURI());
            } catch (IOException e) {
                log.debug("1111111111111111111111111111111111111");
                e.printStackTrace();
            }

            /* #10 ResponseHeader를 생성합니다.
            ResponseUtils.createResponseHeader() 이용해서 생성합니다. responseHeader를 생성합니다.
            */
            try {
                responseHeader = ResponseUtils.createResponseHeader(200, "UTF-8", responseBody.getBytes("utf-8").length);
            } catch (UnsupportedEncodingException e) {
                log.debug("1111111111111111111111111111111111111");
                e.printStackTrace();
            }
        }

        // #12 PrintWriter을 사용 하여 responseHeader, responseBody를 응답합니다.
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))){
            out.write(responseHeader);
            out.write(responseBody);
            // out.write(String.format("%s",responseHeader));
            log.debug("response header : {}",responseHeader);
            // out.write(String.format("%s",responseBody));
            // out.write("\r\n");
            log.debug("response body : {}",responseBody);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if(Objects.nonNull(client) && client.isConnected()){
                    log.debug("client is closed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    client.close();
                }
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }
}
