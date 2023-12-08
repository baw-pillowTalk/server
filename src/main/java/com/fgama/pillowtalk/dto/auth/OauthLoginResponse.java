package com.fgama.pillowtalk.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiredTime;
}
