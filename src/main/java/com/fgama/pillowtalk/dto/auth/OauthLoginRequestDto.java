package com.fgama.pillowtalk.dto.auth;

import com.fgama.pillowtalk.constant.SnsType;
import com.fgama.pillowtalk.domain.Member;
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
    @NotBlank(message = "oauthId 는 필수 입력 값 입니다.")
    private String oauthId;
    @NotBlank(message = "snsType 은 필수 입력 값 입니다.")
    private SnsType snsType;

    public Member toEntity() {
        return Member.builder()
                .oauthId(oauthId)
                .snsType(snsType)
                .build();
    }
}
