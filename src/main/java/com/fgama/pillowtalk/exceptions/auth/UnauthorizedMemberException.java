package com.fgama.pillowtalk.exceptions.auth;

import lombok.Getter;

@Getter
public class UnauthorizedMemberException extends RuntimeException {
    public UnauthorizedMemberException(String message) {
        super(message);
    }
}
