package com.fgama.pillowtalk.constant;

/**
 * enum class 장점
 * - 코드 가시성 및 유지 보수성 : 상수 이름으로 명확히 이해 가능 및 한 곳에서 유지 보수 가능
 * - 싱글톤 관리 : JVM 내에서 하나만 존재한다는 것이 100 % 보장
 * - 타입 안정성 : 특정 범위의 값만 사용 가능 -> 런타임,컴파일 오류 감소
 **/
public enum SnsType {
    NAVER,
    KAKAO,
    GOOGLE,
    APPLE
}
