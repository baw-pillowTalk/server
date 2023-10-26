package com.fgama.pillowtalk.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgama.pillowtalk.auth.SignInWithGoogle;
import com.fgama.pillowtalk.auth.SignInWithKakao;
import com.fgama.pillowtalk.auth.SignInWithNAVER;
import com.fgama.pillowtalk.auth.SigninWithAppleJWT;
import com.fgama.pillowtalk.config.Constants;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.service.MemberService;
import com.nimbusds.jose.JOSEException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.*;

/**
 * 1. 회원 상태 체크 (auto-login)
 * 2. 로그인 (re-login)
 * 3. 로그 아웃(logout)
 */

@Slf4j
@RequiredArgsConstructor
@RestController
public class LoginApi {
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
    public JSendResponse autoLogin() {
//        try {
//            // 데이터불러오기
//
//            //검증
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("signUpState", member.getLoginState());
//            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
//        } catch (Exception e) {
//            return new JSendResponse(Constants.HTTP_FAIL, null);
//        }
    }

    /* auto-login 에서 비회원인 경우 re-login 호출 */

    /***
     * 재로그인(로그인)
     * @param request
     * @return
     */
    @PostMapping("/api/v1/re-login")
    public JSendResponse reLogin(@RequestBody reLoginRequest request) {
    }

    // apple

    /***
     * accessToken 재발급 + refreh 재발급
     * @param request
     * @return
     */
    @PostMapping("/api/v1/renew-access-token")
    public JSendResponse renewAccessToken(@RequestBody RenewAccessTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            Member member = memberService.findMemberByRefreshToken(request.getRefreshToken());
            String snsType = member.getSnsType();
            if (snsType.equals("google")) {
                String responseBody = SignInWithGoogle.getAccessTokenResponseWithRefreshToken(GOOGLE_SNS_CLIENT_ID, GOOGLE_SNS_CLIENT_SECRET, refreshToken);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                //식별자 정보
                String accessToken = jsonNode.get("access_token").asText();
                long expiresIn = Long.parseLong(jsonNode.get("expires_in").asText());
                member.setAccessToken(accessToken);
                member.setExpiresIn(expiresIn);
                memberService.join(member);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("accessToken", accessToken);
                jsonObject.put("refreshToken", refreshToken);
                jsonObject.put("expiresIn", expiresIn);
                return new JSendResponse(SignUpApi.HTTP_SUCCESS, null, jsonObject);
            } else if (snsType.equals("kakao")) {
                //refreshToken으로 accessToken, idToken가져오기
                String userInfoInRefreshToken = SignInWithKakao.renewKakaoRefreshToken(KAKAO_SNS_CLIENT_ID, KAKAO_SNS_CLIENT_SECRET, request.getRefreshToken());
                // responseBody에 있는 정보 읽기
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNodeRefreshToken = objectMapper.readTree(userInfoInRefreshToken);

                String accessToken = jsonNodeRefreshToken.get("access_token").asText();
                long expiresIn = jsonNodeRefreshToken.get("expires_in").asLong();
                member.setAccessToken(accessToken);
                member.setExpiresIn(expiresIn);
                memberService.join(member);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("accessToken", accessToken);
                jsonObject.put("refreshToken", refreshToken);
                jsonObject.put("expiresIn", expiresIn);
                return new JSendResponse(SignUpApi.HTTP_SUCCESS, null, jsonObject);
            } else if (snsType.equals("naver")) {
                String userInfoInRefreshToken = SignInWithNAVER.renewNaverRefreshToken(NAVER_SNS_CLIENT_ID, NAVER_SNS_CLIENT_SECRET, request.getRefreshToken());

                // responseBody에 있는 정보 읽기
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNodeRefreshToken = objectMapper.readTree(userInfoInRefreshToken);

                String accessToken = jsonNodeRefreshToken.get("access_token").asText();
                long expiresIn = jsonNodeRefreshToken.get("expires_in").asLong();
                member.setAccessToken(accessToken);
                member.setExpiresIn(expiresIn);
                memberService.join(member);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("accessToken", accessToken);
                jsonObject.put("refreshToken", refreshToken);
                jsonObject.put("expiresIn", expiresIn);
                return new JSendResponse(SignUpApi.HTTP_SUCCESS, null, jsonObject);
            } else if (snsType.equals("apple")) {
                String jwt = SigninWithAppleJWT.generateSignedJWT(
                        "K5RZZ64DS3",
                        "clonect.com.feeltalk"
                );
                String authorizationCode = SigninWithAppleJWT.validAccessToken(jwt, member.getRefreshToken());

                ObjectMapper mapper = new ObjectMapper();
                AppleRefreshTokenResponse response = mapper.readValue(authorizationCode, AppleRefreshTokenResponse.class);

                String accessToken = response.getAccessToken();
                int expiresIn = response.getExpiresIn();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("accessToken", accessToken);
                jsonObject.put("refreshToken", refreshToken);
                jsonObject.put("expiresIn", expiresIn);
                return new JSendResponse(SignUpApi.HTTP_SUCCESS, null, jsonObject);

            } else {
                return new JSendResponse(SignUpApi.HTTP_FAIL, "유저 없음", null);
            }
        } catch (
                RuntimeException e) {
            return new JSendResponse(SignUpApi.HTTP_FAIL, e.getMessage(), null);
        } catch (
                JsonMappingException e) {
            return new JSendResponse(SignUpApi.HTTP_FAIL, e.getMessage(), null);
        } catch (
                JsonProcessingException e) {
            return new JSendResponse(SignUpApi.HTTP_FAIL, e.getMessage(), null);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
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
    @NoArgsConstructor
    static class reLoginRequest {
        private String idToken; // 구글
        private String authCode; // 구글
        private String authorizationCode; // apple
        private String refreshToken;
        private String state;
        private String code;
        private String snsType;
    }

    @Data
    static class RenewAccessTokenRequest {
        private String refreshToken;
    }
}