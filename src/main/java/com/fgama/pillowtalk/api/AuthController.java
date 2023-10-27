package com.fgama.pillowtalk.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fgama.pillowtalk.config.Constants;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.dto.LoginRequestDto;
import com.fgama.pillowtalk.dto.OauthLoginResponseDto;
import com.fgama.pillowtalk.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 1. 회원 상태 체크 (auto-login)
 * 2. 로그인 (re-login)
 * 3. 로그 아웃(logout)
 */

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final MemberService memberService;

    /**
     * case 01) 처음 애플리케이션을 구동 시켰을 때, 가장 먼저 auto-login API 호출
     * - 호출 시 프론트에서, access, refresh 체크 후, 재 발급 필요 하다면 재 발급 API 요청
     * - 호출 시 프론트에서, access, refresh 체크 후, 재 발급 필요 없다면 auto-login API 요청
     * -> 회원 상태 가져올 수 있음
     * case 02) 비회원인 경우
     * - 비회원인 경우 아무 것도 없을 것 -> 프론트에서 login API 호출 -> case 01) 을 따름
     **/
    @GetMapping("/api/v1/member/status")
    public ResponseEntity<String> getMemberStatus() {
        return new ResponseEntity<>(this.memberService.getMemberStatus(), HttpStatus.OK);
    }

    /* auto-login 에서 비회원인 경우 re-login 호출 */

    /***
     * 재로그인(로그인)
     *
     */
    @PostMapping("/api/v1/login")
    public ResponseEntity<Void> login(
            @RequestBody @Valid LoginRequestDto request) {
        return new ResponseEntity<>(this.memberService.login(request), HttpStatus.OK);
    }

    // apple

    /***
     * accessToken 재발급 + refresh 재발급
     */
    @PostMapping("/api/v1/reissue")
    public ResponseEntity<OauthLoginResponseDto> renewAccessToken(
            @RequestBody @Valid RenewAccessTokenRequest request) {

    }

    /***
     * 로그아웃
     * @param authorizationHeader
     * @return
     */
    @GetMapping("/api/v1/logout")
    public JSendResponse logout(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.logout(accessToken);

            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (RuntimeException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
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

    @Data
    static class RenewAccessTokenRequest {
        private String refreshToken;
    }
}