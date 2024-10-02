package com.nhnacademy.http;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class SimpleHttpServer {

    private final int port;
    private static final int DEFAULT_PORT=8080;

    private final AtomicLong atomicCounter;

    public SimpleHttpServer(){
        this(DEFAULT_PORT);
    }
    public SimpleHttpServer(int port) {
        if(port<=0){
            throw new IllegalArgumentException(String.format("Invalid Port:%d",port));
        }
        this.port = port;
        atomicCounter = new AtomicLong();
    }

    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(port);){

            HttpRequestHandler httpRequestHandlerA = new HttpRequestHandler();
            HttpRequestHandler httpRequestHandlerB = new HttpRequestHandler();

            // #9threadA를 생성하고 시작 합니다.
            Thread threadA = new Thread(httpRequestHandlerA);
            threadA.setName("ThreadA");
            threadA.start();

            // #10threadB를 생성하고 시작 합니다.
            Thread threadB = new Thread(httpRequestHandlerB);
            threadB.setName("ThreadB");
            threadB.start();

            while(true){
                Socket client = serverSocket.accept();
                /* #O11 count값이 짝수이면 httpRequestHandlerA에 client를 추가 합니다.
                           count값이 홀수라면 httpRequestHandlerB에 clinet를 추가 합니다.
                */
                long count = atomicCounter.incrementAndGet();

                log.debug("count: {}", atomicCounter);
                if(count%2 == 0){
                    httpRequestHandlerA.addRequest(client);
                }else{
                    httpRequestHandlerB.addRequest(client);
                }

                count++;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
