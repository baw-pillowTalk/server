package com.fgama.pillowtalk.api;

import com.dreamsecurity.mobileOK.MobileOKException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgama.pillowtalk.auth.SigninWithAppleJWT;
import com.fgama.pillowtalk.constant.HttpResponse;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.dto.MemberDto;
import com.fgama.pillowtalk.dto.adult.AdultAuthenticationTokenDto.AdultAuthenticationRequestDto;
import com.fgama.pillowtalk.dto.adult.AdultAuthenticationVerificationRequestDto;
import com.fgama.pillowtalk.dto.auth.MemberAuthentication;
import com.fgama.pillowtalk.dto.auth.OauthLoginRequestDto;
import com.fgama.pillowtalk.dto.auth.OauthLoginResponse;
import com.fgama.pillowtalk.service.AdultService;
import com.fgama.pillowtalk.service.AuthService;
import com.fgama.pillowtalk.service.JwtService;
import com.fgama.pillowtalk.service.MemberService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
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
 * 5. 성인 인증 관련 로직
 */

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;
    private final AdultService adultService;
    private final MemberService memberService;
    private final JwtService jwtService;


    /**
     * kakao,google,naver,apple ios 소셜 로그인 api
     * - 서비스 access,refresh token 발급
     **/
    @PostMapping("/api/v1/login")
    public JSendResponse login(
            @RequestBody @Valid OauthLoginRequestDto request) {
        OauthLoginResponse oauthLoginResponse = this.authService.login(request);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tokenType", oauthLoginResponse.getTokenType());
        jsonObject.put("accessToken", oauthLoginResponse.getAccessToken());
        jsonObject.put("refreshToken", oauthLoginResponse.getRefreshToken());
        jsonObject.put("expiredTime", oauthLoginResponse.getExpiredTime());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /***
     * 로그아웃
     * - member's refresh token 삭제
     */
    @PostMapping("/api/v1/logout")
    public JSendResponse logout() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.authService.logout());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }


    /***
     * accessToken 재발급 + refresh 재발급
     * - jwt 유효성 검사 x -> 이미 유효하지 않은 상태
     * - jwtAuthenticationFilter 예외 API
     */
    @PostMapping("/api/v1/reissue")
    public JSendResponse reissue(
            HttpServletRequest httpServletRequest
    ) {

        OauthLoginResponse reissueToken = this.authService.reissue(httpServletRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tokenType", reissueToken.getTokenType());
        jsonObject.put("accessToken", reissueToken.getAccessToken());
        jsonObject.put("refreshToken", reissueToken.getRefreshToken());
        jsonObject.put("expiredTime", reissueToken.getExpiredTime());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /**
     * - 회원 탈퇴 API
     **/
    @DeleteMapping("/api/v1/withdraw")
    public JSendResponse withDraw() {
        this.authService.withDraw();
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
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


    /* 본인 인증 요청 */
    @PostMapping("/api/v1/adult/authentication")
    public JSendResponse requestAdultAuthentication(
            @Valid @RequestBody AdultAuthenticationRequestDto request
    ) throws MobileOKException {
        com.dreamsecurity.json.JSONObject result = this.adultService.adultAuthenticationRequest(request);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
    }

    /* 본인 인증 검증 요청 */
    @PostMapping("/api/v1/adult/authentication/verification")
    public JSendResponse requestAdultAuthenticationVerification(
            @Valid @RequestBody AdultAuthenticationVerificationRequestDto request
    ) throws MobileOKException {
        this.adultService.adultAuthenticationVerificationRequest(request);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
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