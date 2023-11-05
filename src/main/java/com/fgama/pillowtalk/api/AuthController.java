package com.fgama.pillowtalk.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fgama.pillowtalk.dto.auth.OauthLoginRequestDto;
import com.fgama.pillowtalk.dto.auth.OauthLoginResponseDto;
import com.fgama.pillowtalk.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 1. 로그인 (re-login)
 * 2. 로그 아웃(logout)
 * 3. 엑시스,리프레시 재발급(reissue)
 * 4. 회원 탈퇴(withdraw)
 */

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;

    /**
     * kakao,google,naver 소셜 로그인 api
     * - 서비스 access,refresh token 발급
     **/
    @PostMapping("/api/v1/login")
    public ResponseEntity<OauthLoginResponseDto> login(
            @RequestBody @Valid OauthLoginRequestDto request) {
        return new ResponseEntity<>(this.authService.login(request), HttpStatus.OK);
    }

    /***
     * 로그아웃
     * - member's refresh token 삭제
     */
    @PostMapping("/api/v1/logout")
    public ResponseEntity<Long> logout() {
        return new ResponseEntity<>(this.authService.logout(), HttpStatus.OK);
    }


    /***
     * accessToken 재발급 + refresh 재발급
     * - jwt 유효성 검사 x -> 이미 유효하지 않은 상태
     * - jwtAuthenticationFilter 예외 API
     */
    @PostMapping("/api/v1/reissue")
    public ResponseEntity<OauthLoginResponseDto> renewAccessToken(
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(this.authService.reissue(httpServletRequest));
    }

    /**
     * - 회원 탈퇴 API
     **/
    @DeleteMapping("/api/v1/withdraw")
    public ResponseEntity<Void> withDraw() {
        this.authService.withDraw();
        return ResponseEntity.ok().build();
    }


    @Data
    public static class AppleRefreshTokenResponse {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private int expiresIn;

        @JsonProperty("id_token")
        private String idToken;

        @JsonProperty("token_type")
        private String tokenType;
    }
}