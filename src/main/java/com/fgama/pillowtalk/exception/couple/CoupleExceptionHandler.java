package com.fgama.pillowtalk.exception.couple;

import com.fgama.pillowtalk.api.CoupleController;
import com.fgama.pillowtalk.exception.ErrorMessage;
import com.fgama.pillowtalk.exception.member.MemberNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = CoupleController.class)
public class CoupleExceptionHandler {
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorMessage> memberNotFoundExceptionHandler(
            MemberNotFoundException exception
    ) {
        return ResponseEntity.badRequest().body(ErrorMessage.of(exception, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(CoupleAlreadyExistException.class)
    public ResponseEntity<ErrorMessage> coupleAlreadyExistExceptionHandler(
            CoupleAlreadyExistException exception
    ) {
        return ResponseEntity.badRequest().body(ErrorMessage.of(exception, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(CoupleNotFoundException.class)
    public ResponseEntity<ErrorMessage> coupleNotFoundExceptionHandler(
            CoupleNotFoundException exception
    ) {
        return ResponseEntity.badRequest().body(ErrorMessage.of(exception, HttpStatus.BAD_REQUEST));
    }
}
