package com.fgama.pillowtalk.exception.signup;

import com.fgama.pillowtalk.api.SignupController;
import com.fgama.pillowtalk.dto.JSendResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = SignupController.class)
public class SignupExceptionHandler {
    @ExceptionHandler(MemberStateNotEqualException.class)
    public ResponseEntity<JSendResponse> memberStateNotEqualExceptionHandler(
            MemberStateNotEqualException exception
    ) {
        return ResponseEntity.ok(JSendResponse.of(exception));
    }
}
