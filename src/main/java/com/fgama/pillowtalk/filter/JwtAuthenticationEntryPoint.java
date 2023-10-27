package com.fgama.pillowtalk.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgama.pillowtalk.exception.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * - JwtAuthenticationEntryPoint :  SecurityFilterChain 을 지나면서 발생한 AuthenticationException 을 처리하는 역할
 * - commence() 메소드 구현
 **/
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException
    ) throws IOException, ServletException {
        log.error("Authentication Exception Occurs!");
        this.sendAuthenticationExceptionResponse(request, response, authException);
    }

    private void sendAuthenticationExceptionResponse(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 404 Unauthorized error
        response.setContentType("application/json,charset=utf-8");


        try (OutputStream os = response.getOutputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(os, ErrorMessage.of(authException, HttpStatus.UNAUTHORIZED));
            os.flush();
        }
    }
}
