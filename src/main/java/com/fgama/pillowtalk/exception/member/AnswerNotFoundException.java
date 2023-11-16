package com.fgama.pillowtalk.exception.member;

import lombok.Getter;

@Getter
public class AnswerNotFoundException extends RuntimeException {
    public AnswerNotFoundException(String message) {
        super(message);
    }
}
