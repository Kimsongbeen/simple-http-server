package com.nhnacademy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.nhnacademy.http.request.HttpRequest;
import com.nhnacademy.http.request.HttpRequestImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class HttpRequestImplTest {
    static final long TEST_PORT = 9999;

    static HttpRequest request;

    static Socket client = Mockito.mock(Socket.class);

    final static String CRLF="\r\n";

    @BeforeAll
    static void setUp() throws IOException {
        String data = "id=bee&age=22&name=beebee";

        //utf-8 encode
        data = URLEncoder.encode(data,StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("POST /index.html HTTP/1.1%s",CRLF));
        sb.append(String.format("Host: localhost:%d%s",TEST_PORT,CRLF));
        sb.append(String.format("Content-Type: application/x-www-form-urlencoded; charset=UTF-8%s",CRLF));
        sb.append(String.format("Content-Length: %d%s",data.getBytes("utf-8").length,CRLF));
        sb.append(CRLF);
        sb.append(data);

        InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        Mockito.when(client.getInputStream()).thenReturn(inputStream);
        request = new HttpRequestImpl(client);
    }

    @Test
    void getMethod() {
        String method = request.getMethod();
        assertEquals("POST",method.toUpperCase());
    }

    @Test
    void getParameterById() {
        String param = request.getParameter("id");
        assertEquals("bee",param);
    }
    @Test
    void getParameterByName() {
        String param = request.getParameter("name");
        assertEquals("beebee",param);
    }

    @Test
    void getParameterByAge() {
        String param = request.getParameter("age");
        assertEquals("22",param);
    }

    @Test
    void getParameterMap() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("id","bee");
        expected.put("age","22");
        expected.put("name","beebee");

        @SuppressWarnings("rawtypes")
        Map actual = request.getParameterMap();
        assertEquals(expected,actual);
    }

    @Test
    void getHeader_contentType() {
        String contentType = request.getHeader("Content-Type");
        assertEquals("Content-Type", contentType);
        // log.debug("Content-Type:{}",contentType);
        // Assertions.assertTrue(contentType.contains("application/x-www-form-urlencoded"));
    }

    @Test
    void getHeader_charset() {
        String charset = request.getHeader("charset");
        log.debug("charset:{}",charset);
        Assertions.assertTrue(charset.toLowerCase().contains("utf-8"));
    }

    @Test
    void getRequestURI() {
        String uri = request.getRequestURI();
        log.debug("uri:{}", uri);
        Assertions.assertTrue(uri.contains("index.html"));
    }

}