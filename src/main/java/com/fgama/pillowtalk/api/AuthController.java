package com.fgama.pillowtalk.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgama.pillowtalk.auth.SigninWithAppleJWT;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.MemberDto;
import com.fgama.pillowtalk.dto.auth.MemberAuthentication;
import com.fgama.pillowtalk.dto.auth.OauthLoginRequestDto;
import com.fgama.pillowtalk.dto.auth.OauthLoginResponse;
import com.fgama.pillowtalk.service.AuthService;
import com.fgama.pillowtalk.service.JwtService;
import com.fgama.pillowtalk.service.MemberService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.fgama.pillowtalk.auth.SigninWithAppleJWT.getAppleUserInfo;
import static com.fgama.pillowtalk.constant.SnsType.APPLE;

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
    private final MemberService memberService;
    private final JwtService jwtService;


    /**
     * kakao,google,naver,apple ios 소셜 로그인 api
     * - 서비스 access,refresh token 발급
     **/
    @PostMapping("/api/v1/login")
    public ResponseEntity<OauthLoginResponse> login(
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
    public ResponseEntity<OauthLoginResponse> reissue(
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

    /***
     * 안드로이드용 애플로그인 시 callback함수로 애플에서 "login/apple" url로 보내줌
     * code로 회원데이터 얻을수있음
     * 콜백으로온 state는 회원가입시 클라이언트에서 보내는 state코드와 비교하여 인증하는 용도로 사용
     */
    @RequestMapping(value = "/api/v1/login/apple")
    public OauthLoginResponse oauth_apple_v1(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state
    ) throws Exception { // android에서 애플가입 웹뷰
        try {
            //clientSecret만들기
            String jwt = SigninWithAppleJWT.generateSignedJWT(
                    "K5RZZ64DS3",
                    "clonect.com.feeltalk"
            );
            //정보가져오기
            String authorizationCode = SigninWithAppleJWT.exchangeAuthorizationCode(jwt, code);

            //정보추출
            ObjectMapper mapper = new ObjectMapper();
            MemberDto.AppleTokenResponse response = mapper.readValue(authorizationCode, MemberDto.AppleTokenResponse.class);

            String idToken = response.getIdToken();
            JsonObject appleUserInfo = getAppleUserInfo(idToken);
            JsonElement appleAlg = appleUserInfo.get("sub"); // 유저 oauthId
            String oauthId = appleAlg.getAsString();
            Member member = memberService.findMemberByOauthIdAndSnsType(oauthId, APPLE);
            if (member == null) {
                // 회원 가입
                Member savedMember = authService.saveMemberFromAppleAndroid(oauthId);
                return getOauthLoginResponseDto(savedMember);
            }
            // jwt 발급 (이미 회원가입 한 경우)
            return this.jwtService.createServiceToken(member);

        } catch (JOSEException | JsonProcessingException e) {
            log.info("애플 회원가입/재로그인 실패" + e.getMessage());
            throw e;
        }
    }

    private OauthLoginResponse getOauthLoginResponseDto(Member savedMember) {
        SecurityContextHolder.getContext().setAuthentication(new MemberAuthentication(savedMember)); // 인증 객체 생성
        OauthLoginResponse serviceToken = this.jwtService.createServiceToken(savedMember); // 서비스 토큰 생성
        savedMember.setRefreshToken(serviceToken.getRefreshToken());
        return serviceToken;
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