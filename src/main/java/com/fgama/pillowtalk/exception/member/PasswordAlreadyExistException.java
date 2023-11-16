package com.fgama.pillowtalk.exception.member;

public class PasswordAlreadyExistException extends RuntimeException {
    public PasswordAlreadyExistException(String message) {
        super(message);
    }
}
