package com.fgama.pillowtalk.exception.member;

import lombok.Getter;

@Getter
public class PasswordNotMatchException extends RuntimeException {
    public PasswordNotMatchException(String message) {
        super(message);
    }
}
