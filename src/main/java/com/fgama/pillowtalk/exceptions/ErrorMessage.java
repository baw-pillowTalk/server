package com.fgama.pillowtalk.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorMessage {
    private int code;
    private String errorSimpleName;
    private String message;
    private LocalDateTime timeStamp;


    public ErrorMessage(Exception exception, HttpStatus httpStatus) {
        this.code = httpStatus.value();
        this.errorSimpleName = exception.getClass().getSimpleName();
        this.message = exception.getMessage();
        this.timeStamp = LocalDateTime.now();
    }

    public static ErrorMessage of(Exception exception, HttpStatus httpStatus) {
        return new ErrorMessage(exception, httpStatus);
    }
}
