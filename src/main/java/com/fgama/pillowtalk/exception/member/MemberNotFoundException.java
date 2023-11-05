package com.fgama.pillowtalk.exception.member;

import lombok.Getter;

@Getter
public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {
        super("not fount member by accessToken");
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}
