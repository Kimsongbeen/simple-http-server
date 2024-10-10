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

package com.nhnacademy.http.context;

import com.nhnacademy.http.context.exception.ObjectNotFoundException;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// #2 - Context를 구현합니다.
public class ApplicationContext  implements Context{
    ConcurrentMap<String, Object> objectMap;

    public ApplicationContext() {
        this.objectMap = new ConcurrentHashMap<>();
    }

    @Override
    public void setAttribute(String name, Object object) {
        objectNameCheck(name);
        objectValueCheck(object);
        objectMap.put(name, object);
    }

    @Override
    public void removeAttribute(String name) {
        objectNameCheck(name);
        objectMap.remove(name);
    }

    @Override
    public Object getAttribute(String name) {
        //object가 존재하지 않는다면 ObjectNotFoundException 예외가 발생합니다.

        objectNameCheck(name);
        Object object = objectMap.get(name);
        if(Objects.isNull(object)){
            throw new ObjectNotFoundException(name);
        }
        return object;
    }

    private void objectNameCheck(String name){
        if(Objects.isNull(name) || name.length() == 0){
            throw new IllegalArgumentException(name);
        }
    }

    private void objectValueCheck(Object o){
        if(Objects.isNull(o)){
            throw new IllegalArgumentException("value is null");
        }
    }
}
