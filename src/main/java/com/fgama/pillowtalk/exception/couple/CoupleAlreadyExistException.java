package com.fgama.pillowtalk.exception.couple;

import lombok.Getter;

@Getter
public class CoupleAlreadyExistException extends RuntimeException {
    public CoupleAlreadyExistException(String message) {
        super(message);
    }
}
