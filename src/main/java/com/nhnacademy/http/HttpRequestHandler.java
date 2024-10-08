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

import com.nhnacademy.http.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class HttpRequestHandler implements Runnable {

    private final RequestChannel requestChannel;

    public HttpRequestHandler(RequestChannel requestChannel) {
        this.requestChannel = requestChannel;
    }

    @Override
    public void run() {
        Executable httpJob = requestChannel.getHttpJob();
        try {
            httpJob.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        run();
    }
}
