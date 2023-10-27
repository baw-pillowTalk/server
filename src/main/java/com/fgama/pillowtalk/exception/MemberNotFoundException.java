package com.fgama.pillowtalk.exception;

public class MemberNotFoundException extends NullPointerException {
    public MemberNotFoundException() {
        super("not fount member by accessToken");
    }

    public MemberNotFoundException(String s) {
        super(s);
    }
}
