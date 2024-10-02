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

 package com.nhnacademy.http;

 import lombok.extern.slf4j.Slf4j;
 
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
import java.util.Objects;
 
 @Slf4j
 public class SimpleHttpServer {
 
     private final int port;
     private static final int DEFAULT_PORT=8080;
     private static final String CRLF = "\r\n";
     private final ServerSocket serverSocket;
 
     public SimpleHttpServer(){
         // #2 기본 port는 8080을 사용합니다.
         this(DEFAULT_PORT);
     }
 
     public SimpleHttpServer(int port) {
         // #1 port range <=0 IllegalArgumentException 예외가 발생 합니다.
         if(port <= 0){
            throw new IllegalArgumentException();
         }

         this.port = port;

         try {
            serverSocket = new ServerSocket(this.port);
         } catch (Exception e) {
            throw new IllegalArgumentException();
         }
     }
 
     public void start() throws IOException {
 
         StringBuilder requestBuilder = new StringBuilder();
 
         try{
 
             while(true){
                 // #3 client가 연결될 때 까지 대기 합니다.
                 Socket client = serverSocket.accept();

                 try(
                    // #4 입출력을 위해서  Reader, Writer를 선언 합니다.
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                    ) {
                         log.debug("------HTTP-REQUEST_start()");
                         while (true) {
                             String line = bufferedReader.readLine();
                             // #5  requestBuilder에 append 합니다.
                             requestBuilder.append(line);
                             log.debug("{}", line);
 
                             // #6 break(종료) 조건을 구현 line is null or line length == 0
                             if(Objects.isNull(line) || line.length() == 0){
                                break;
                             }
                         }
                         log.debug("------HTTP-REQUEST_end()");
 
                         // #7 clinet에 응답할 html을 작성합니다.
                         StringBuilder responseBody = new StringBuilder();
                         responseBody.append("<html>");
                         responseBody.append("<body>");
                         responseBody.append("<h1>hello java</h1>");
                         responseBody.append("</body>");
                         responseBody.append("</html>");
                         /*
                             <html>
                                 <body>
                                     <h1>hello hava</h1>
                                 </body>
                             </html>
                         */
 
 
                         StringBuilder responseHeader = new StringBuilder();
 
                         // #8 HTTP/1.0 200 OK
                         responseHeader.append(String.format("HTTP/1.0 200 OK%s", CRLF));
 
                         responseHeader.append(String.format("Server: HTTP server/0.1%s",CRLF));
 
                         // #9 Content-type: text/html; charset=UTF-8"
                         responseHeader.append(String.format("Content-type: text/html; charset=%s%s","UTF-8", CRLF));
 
                         responseHeader.append(String.format("Connection: Closed%s", CRLF));
 
                         // #10 responseBody의  Content-Length를 설정 합니다.
                         responseHeader.append(String.format("Content-lenght: %d %s%s", responseBody.toString().getBytes().length, CRLF, CRLF));
 
                         // #11 write Response Header
                         bufferedWriter.write(responseHeader.toString());
 
                         // #12 write Response Body
                         bufferedWriter.write(responseBody.toString());
 
                         // #13 buffer에 등록된 Response (header, body) flush 합니다.(socket을 통해서 clent에 응답 합니다.)
                         bufferedWriter.flush();
 
                         log.debug("header:{}",responseHeader);
                         log.debug("body:{}",responseBody);
 
                 }catch (IOException e){
                     log.error("sock error : {}",e);
                 }finally {
                     // #14 Client Socket Close
                     client.close();
                 }
             }//end while
 
         }catch(IOException e){
            throw new IllegalArgumentException();
         } 
     }
 }
 