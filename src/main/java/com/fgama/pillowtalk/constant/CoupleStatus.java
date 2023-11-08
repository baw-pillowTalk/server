package com.fgama.pillowtalk.constant;

import lombok.Getter;

/**
 * - 커플 상태
 * Available : 회원 사용 상태
 * Unavailable : 탈퇴한 회원이 쓰던 연인 데이터
 * BreakUp : 연인 끊기
 **/
@Getter
public enum CoupleStatus {
    AVAILABLE, UNAVAILABLE, BREAK_UP
}
