package com.fgama.pillowtalk.exception.signup;

import com.fgama.pillowtalk.api.SignupController;
import com.fgama.pillowtalk.exception.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = SignupController.class)
public class SignupExceptionHandler {
    @ExceptionHandler(MemberStateNotEqualException.class)
    public ResponseEntity<ErrorMessage> memberStateNotEqualExceptionHandler(
            MemberStateNotEqualException exception
    ) {
        return ResponseEntity.badRequest()
                .body(ErrorMessage.of(exception, HttpStatus.BAD_REQUEST));
    }
}
