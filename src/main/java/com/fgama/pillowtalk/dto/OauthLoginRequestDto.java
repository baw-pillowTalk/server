package com.fgama.pillowtalk.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * - 회원 로그인 및 access & refresh token 재발급 응답 dto
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OauthLoginRequestDto {
    @NotBlank(message = "oauthId 는 입력 필수 값입니다.")
    private String oauthId;
    @NotBlank
    private String snsType;
}
