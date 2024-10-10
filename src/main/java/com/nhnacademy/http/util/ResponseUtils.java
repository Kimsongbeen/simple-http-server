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

package com.nhnacademy.http.util;

import java.io.*;
import java.net.URL;
import java.util.Objects;

public class ResponseUtils {
    public static final String DEFAULT_404 = "/404.html";
    public static final String DEFAULT_405 = "/405.html";
    private static final String CRLF = "\r\n";
    private ResponseUtils(){}

    public enum HttpStatus{
        OK(200, "OK"),
        NOT_FOUND(404, "Not Found"),
        UNKOWN(-1, "Unknown Status"),
        METHOD_NOT_ALLOWED(405, "Method Not Allowed");

        private final int code;
        private final String description;

        HttpStatus(int code, String description){
            this.code = code;
            this.description = description;
        }

        public int getCode(){
            return code;
        }

        public String getDescription(){
            return description;
        }

        public static HttpStatus getStatusFromCode(int code){
            for(HttpStatus status : HttpStatus.values()){
                if(status.getCode() == code){
                    return status;
                }
            }
            return UNKOWN;
        }
    }

    public static boolean isExist(String filePath){
        URL url = ResponseUtils.class.getResource(filePath);
        return Objects.nonNull(url);
    }
    public static String tryGetBodyFormFile(String filePath) throws IOException {
        StringBuilder responseBody = new StringBuilder();
        try(InputStream inputStream = ResponseUtils.class.getResourceAsStream(filePath);
            BufferedReader reader =  new BufferedReader(new InputStreamReader(inputStream,"UTF-8"))){
            while(true) {
                String line = reader.readLine();
                if(Objects.isNull(line)){
                    break;
                }
                responseBody.append(line);
            }
        }
        return responseBody.toString();
    }

    public static String createResponseHeader(int httpStatusCode, String charset, int contentLength ){
        StringBuilder responseHeader = new StringBuilder();
        responseHeader.append(String.format("HTTP/1.0 %d %s%s",httpStatusCode,HttpStatus.getStatusFromCode(httpStatusCode).getDescription(), CRLF));
        responseHeader.append(String.format("Server: HTTP server/0.1%s",CRLF));
        responseHeader.append(String.format("Content-type: text/html; charset=%s%s",charset,CRLF));
        responseHeader.append(String.format("Connection: Closed%s",CRLF));
        responseHeader.append(String.format("Content-Length:%d %s%s",contentLength,CRLF,CRLF));
        return responseHeader.toString();
    }

}
