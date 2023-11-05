package com.fgama.pillowtalk.exception.couple;

import lombok.Getter;

@Getter
public class CoupleNotFoundException extends RuntimeException {
    public CoupleNotFoundException(String message) {
        super(message);
    }
}
