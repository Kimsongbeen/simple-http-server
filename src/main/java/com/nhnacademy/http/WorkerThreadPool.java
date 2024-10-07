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

import com.nhnacademy.http.channel.RequestChannel;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
@Slf4j
public class WorkerThreadPool {
    // @SuppressWarnings("unused")
    private final int poolSize;
    private final static int DEFAULT_POOL_SIZE=4;
    private final Thread[] workerThreads;
    // @SuppressWarnings("unused")
    private final RequestChannel requestChannel;

    public WorkerThreadPool(RequestChannel requestChannel){
        this(DEFAULT_POOL_SIZE, requestChannel);
    }
    public WorkerThreadPool(int poolSize, RequestChannel requestChannel) {
        // #1 poolSize <1 다면 IllegalArgumentException이 발생합니다.
        if(poolSize < 1){
            throw new IllegalArgumentException("poolSize > 1");
        }

        // #2 requestChannel null check
        if(Objects.isNull(requestChannel)){
            throw new IllegalArgumentException("requestChannel must not null");
        }

        // #3 pooSize, requestChannel 초기화
        this.poolSize = poolSize;
        this.requestChannel = requestChannel;

        // #4 requestChannel을 이용하여 httpRequestHandler 객체를 생성 합니다.
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(requestChannel);

        // #5 workerThreads를 초기화 합니다. poolSize 만큼 Thread를 생성 합니다.
        workerThreads = new Thread[poolSize];

        for(int i=0; i<poolSize; i++){
            // #6 workerThread 생성및 이름 설정 :  thread-1,thread-2, thread-3 ...
            workerThreads[i] = new Thread(httpRequestHandler);
            workerThreads[i].setName(String.format("thread-%d", i));
        }
    }
    public synchronized void start(){
        // #7 workerThreads에 초가화된 모든 Thread를 start 합니다.
        for(Thread thread : workerThreads){
            log.info("thread nane : {}", thread.getName());
            thread.start();
        }
    }

    public synchronized void stop(){

        /* #8 interrupt()를 실행해서 thread를 종료 합니다.
            - thread가 종료되는 과정에서 동기화 되어야 합니다.
         */
        for(Thread thread : workerThreads){
            if(Objects.nonNull(thread) && thread.isAlive())
                thread.interrupt();
        }

        // #9 join()를 호출해서 모든 thread가 종료될 떄 까지 대기 합니다.
        for(Thread thread : workerThreads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
