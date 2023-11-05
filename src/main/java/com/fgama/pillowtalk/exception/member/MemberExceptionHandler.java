package com.fgama.pillowtalk.exception.member;

import com.fgama.pillowtalk.api.MemberController;
import com.fgama.pillowtalk.exception.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = MemberController.class)
public class MemberExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorMessage> memberNotFoundExceptionHandler(
            MemberNotFoundException exception
    ) {
        return ResponseEntity.badRequest()
                .body(ErrorMessage.of(exception, HttpStatus.BAD_REQUEST));
    }
}
