package com.fgama.pillowtalk.exception.global;

import lombok.Getter;

/**
 * - 유저의 추가 적인 회원 가입 과정이 필요할 때 발생하는 예외
 **/

@Getter
public class MemberNeedExtraSignupException extends RuntimeException {
    public MemberNeedExtraSignupException(String message) {
        super(message);
    }
}
