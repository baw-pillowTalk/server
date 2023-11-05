package com.fgama.pillowtalk.exception.auth;

import lombok.Getter;

/**
 * - 인증 관련 예외 클래스
 **/
@Getter
public class UnauthorizedMemberException extends RuntimeException {
    public UnauthorizedMemberException(String message) {
        super(message);
    }
}
