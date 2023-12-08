package com.fgama.pillowtalk.exception.couple;

import lombok.Getter;

@Getter
public class CoupleNeedExtraSignupException extends RuntimeException {
    public CoupleNeedExtraSignupException(String message) {
        super(message);
    }
}
