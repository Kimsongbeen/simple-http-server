package com.nhnacademy.http.service;

import java.io.IOException;
import java.io.PrintWriter;
import lombok.extern.slf4j.Slf4j;

import com.nhnacademy.http.request.HttpRequest;
import com.nhnacademy.http.response.HttpResponse;
import com.nhnacademy.http.util.ResponseUtils;

@Slf4j
public class NotFoundHttpService implements HttpService{
    @Override
    public void doGet(HttpRequest httpRequest, HttpResponse httpResponse){
        String responseBody = null;

        try {
            responseBody = ResponseUtils.tryGetBodyFormFile(ResponseUtils.DEFAULT_404);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String responseHeader = ResponseUtils.createResponseHeader(ResponseUtils.HttpStatus.NOT_FOUND.getCode(), "UTF-8", responseBody.length());

        try (PrintWriter bufferedWriter = httpResponse.getWriter();){
            bufferedWriter.write(responseHeader);
            bufferedWriter.write(responseBody);
            bufferedWriter.flush();
            log.debug("body: {}", responseBody.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
