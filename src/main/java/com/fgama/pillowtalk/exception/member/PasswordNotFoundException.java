package com.fgama.pillowtalk.exception.member;

import lombok.Getter;

@Getter
public class PasswordNotFoundException extends RuntimeException {
    public PasswordNotFoundException(String message) {
        super(message);
    }
}
