package com.fgama.pillowtalk.exception.global;

import com.fgama.pillowtalk.dto.JSendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * - 특정 도메인이 아닌 전역적으로 발생하는 에외 처리을 위한 클래스
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JSendResponse> illegalArgumentExceptionHandler(
            IllegalArgumentException exception) {
        log.warn("IllegalArgumentException Occurs");
        return ResponseEntity.ok(JSendResponse.of(exception));
    }

    @ExceptionHandler(MemberNeedExtraSignupException.class)
    public ResponseEntity<JSendResponse> memberNeedExtraSignupExceptionHandler(
            MemberNeedExtraSignupException exception
    ) {
        log.warn("MemberNeedExtraSignupException Occurs");
        return ResponseEntity.ok(JSendResponse.of(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JSendResponse> exceptionHandler(
            Exception exception
    ) {
        log.warn("Exception Occurs");
        return ResponseEntity.ok(JSendResponse.of(exception));
    }
}
