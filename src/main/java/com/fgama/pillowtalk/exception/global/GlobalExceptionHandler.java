package com.fgama.pillowtalk.exception.global;

import com.fgama.pillowtalk.exception.ErrorMessage;
import com.fgama.pillowtalk.exception.ErrorMessage.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * - 특정 도메인이 아닌 전역적으로 발생하는 에외 처리을 위한 클래스
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgumentExceptionHandler(
            IllegalArgumentException exception) {
        log.warn("IllegalArgumentException Occurs");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.of(exception, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(MemberNeedExtraSignupException.class)
    public ResponseEntity<?> memberNeedExtraSignupExceptionHandler(
            MemberNeedExtraSignupException exception
    ) {
        log.warn("MemberNeedExtraSignupException Occurs");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.of(exception, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exceptionHandler(
            Exception exception
    ) {
        log.warn("Exception Occurs");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.of(exception, HttpStatus.BAD_REQUEST));
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders httpHeaders,
            HttpStatus httpStatus,
            WebRequest request
    ) {
        log.warn("MethodArgumentNotValidException Occurs!");

        List<ValidationError> validationErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ValidationError::of)
                .collect(Collectors.toList());

        return ResponseEntity.status(httpStatus.value())
                .body(ErrorMessage.of(exception, httpStatus, validationErrors));
    }
}
