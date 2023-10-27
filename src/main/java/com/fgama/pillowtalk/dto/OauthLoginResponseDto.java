package com.fgama.pillowtalk.dto;

import lombok.*;

/**
 * - 회원 로그인 및 access & refresh token 재발급 응답 dto
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OauthLoginResponseDto {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private String expiredTime;
}
