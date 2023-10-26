package com.fgama.pillowtalk.exceptions;

import java.util.function.Supplier;

public class MemberNotFoundException extends NullPointerException {
    public MemberNotFoundException() {
        super("not fount member by accessToken");
    }

    public MemberNotFoundException(String s) {
        super(s);
    }
}
