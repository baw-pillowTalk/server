package com.fgama.pillowtalk.dto.auth;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OauthLoginResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiredTime;
}
