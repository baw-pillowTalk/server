package com.fgama.pillowtalk.exception.auth;

import com.fgama.pillowtalk.api.AuthController;
import com.fgama.pillowtalk.exception.ErrorMessage;
import com.fgama.pillowtalk.exception.member.MemberNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(UnauthorizedMemberException.class)
    public ResponseEntity<ErrorMessage> unauthorizedMemberExceptionHandler(
            UnauthorizedMemberException exception
    ) {
        return ResponseEntity.badRequest().body(
                ErrorMessage.of(exception, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorMessage> memberNotFoundExceptionHandler(
            MemberNotFoundException exception
    ) {
        return ResponseEntity.badRequest().body(
                ErrorMessage.of(exception, HttpStatus.BAD_REQUEST));
    }
}
