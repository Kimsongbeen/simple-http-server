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

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

@Slf4j
public class HttpJob implements Executable {
    private final Socket client;
    private static final String CRLF="\r\n";

    public HttpJob(Socket client) {
        if(Objects.isNull(client)){
            throw new IllegalArgumentException("client Socket is null");
        }
        this.client = client;
    }

    public Socket getClient() {
        return client;
    }

    @Override
    public void execute(){

        // #23 HttpJob는 execute() method를 구현 합니다.
        //<html><body><h1>thread-0:hello java</h1></body>
        //<html><body><h1>thread-1:hello java</h1></body>
        //<html><body><h1>thread-2:hello java</h1></body>
        //....

        StringBuilder requestBuilder = new StringBuilder();
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        ){
            while(true){
                String line = bufferedReader.readLine();
                requestBuilder.append(line);

                log.debug("line: {}", line);

                if(Objects.isNull(line) || line.length() == 0){
                    break;
                }
            }

            StringBuilder responseBody = new StringBuilder();
            responseBody.append("<html>");
                responseBody.append("<body>");
                    responseBody.append(String.format("<h1>{%s}hello java</h1>",Thread.currentThread().getName()));
                responseBody.append("</body>");
            responseBody.append("</html>");

            StringBuilder responseHeader = new StringBuilder();
            responseHeader.append(String.format("HTTP/1.0 200 OK%s", CRLF));
            responseHeader.append(String.format("Server: HTTP server/0.1%s",CRLF));
            responseHeader.append(String.format("Content-type: text/html; charset=%s%s","UTF-8",CRLF));
            responseHeader.append(String.format("Connection: Closed%s",CRLF));
            responseHeader.append(String.format("Content-Length:%d %s%s",responseBody.length(), CRLF,CRLF));

            bufferedWriter.write(responseHeader.toString());
            bufferedWriter.write(responseBody.toString());
            bufferedWriter.flush();

            client.close();
        }catch (IOException e){
            log.error("server error:{}",e);
        }
    }
}
