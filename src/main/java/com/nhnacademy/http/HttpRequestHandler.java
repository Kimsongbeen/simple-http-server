package com.nhnacademy.http;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

@Slf4j
public class HttpRequestHandler implements Runnable {

    private final Queue<Socket> requestQueue;
    private final int MAX_QUEUE_SIZE=10;

    public HttpRequestHandler() {
        // #1 requestQueue를 초기화 합니다. Java에서 Queue의 구현체인 LinkedList를 사용 합니다.
        requestQueue = new LinkedList<>();
    }

    public synchronized void addRequest(Socket client){

        /*  #2 queueSize >= MAX_QUEUE_SIZE 대기 합니다.
            즉 queue에 데이터가 소비될 때 까지 client Socket을 Queue에 등록하는 작업을 대기 합니다.
        */
        while(requestQueue.size() >= MAX_QUEUE_SIZE){
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }

        // #3 requestQueue에 client를 추가 합니다.
        requestQueue.add(client);

        // #4 대기하고 있는 Thread를 깨웁니다.
        notifyAll();
    }

    public synchronized Socket getRequest(){

        // #5 requestQueue가 비어 있다면 대기 합니다.
        while(requestQueue.isEmpty()){
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // #6 대기하고 있는 Thread를 깨우고, requestQueue에서 client를 반환 합니다.
        notifyAll();
        return requestQueue.poll();
    }

    @Override
    public void run() {
        // #7 getRequest()를 호출하여 client를 requestQueue로 부터 얻습니다., requestQueue가 비어있다면 대기 합니다.
        Socket client = getRequest();


        StringBuilder requestBuilder = new StringBuilder();
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        ) {

            while (true) {
                String line = bufferedReader.readLine();
                requestBuilder.append(line);
                log.debug("line:{}", line);
                if (Objects.isNull(line) || line.length() == 0) {
                    break;
                }
            }

            StringBuilder responseBody = new StringBuilder();
            responseBody.append("<html>");
            responseBody.append("<body>");
            responseBody.append("<h1>hello 12345</h1>");
            responseBody.append("</body>");
            responseBody.append("</html>");

            StringBuilder responseHeader = new StringBuilder();

            responseHeader.append(String.format("HTTP/1.0 200 OK%s",System.lineSeparator()));
            responseHeader.append(String.format("Server: HTTP server/0.1%s",System.lineSeparator()));
            responseHeader.append(String.format("Content-type: text/html; charset=%s%s","UTF-8",System.lineSeparator()));
            responseHeader.append(String.format("Connection: Closed%s",System.lineSeparator()));
            responseHeader.append(String.format("Content-Length:%d %s%s",responseBody.length(),System.lineSeparator(),System.lineSeparator()));

            bufferedWriter.write(responseHeader.toString());
            bufferedWriter.write(responseBody.toString());
            bufferedWriter.flush();
            client.close();
        }catch (IOException e){
            log.error("server error:{}",e);
        }finally {
            try {
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // #8 client에 응답하고 run() method를 호출 합니다., requestQueue가 비어있지 않다면 run() 실행합니다.
        if(!requestQueue.isEmpty()){
            run();
        }
    }
}
