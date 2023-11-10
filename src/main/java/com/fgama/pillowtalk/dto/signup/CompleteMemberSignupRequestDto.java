package com.fgama.pillowtalk.dto.signup;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMemberSignupRequestDto {
    private String state;
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{1,10}$", message = "닉네임 입력 형식에 일치하지 않습니다.")
    private String nickname;
    private Boolean marketingConsent;
    @NotBlank
    private String fcmToken;
}
