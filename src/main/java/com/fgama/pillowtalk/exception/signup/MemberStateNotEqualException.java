package com.fgama.pillowtalk.exception.signup;

import lombok.Getter;

@Getter
public class MemberStateNotEqualException extends RuntimeException {
    public MemberStateNotEqualException(String message) {
        super(message);
    }
}
