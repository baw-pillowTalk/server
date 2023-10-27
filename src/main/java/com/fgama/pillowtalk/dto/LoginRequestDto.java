package com.fgama.pillowtalk.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private String expiredTime;
}
