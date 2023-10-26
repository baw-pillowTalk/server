package com.fgama.pillowtalk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgama.pillowtalk.auth.SignInWithGoogle;
import com.fgama.pillowtalk.auth.SignInWithKakao;
import com.fgama.pillowtalk.auth.SignInWithNAVER;
import com.fgama.pillowtalk.auth.SigninWithAppleJWT;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.dto.MemberDto;
import com.fgama.pillowtalk.service.MemberService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static com.fgama.pillowtalk.auth.SigninWithAppleJWT.getAppleUserInfo;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SignUpApi {
    public static final String HTTP_SUCCESS = "success";
    public static final String HTTP_FAIL = "fail";
    private final MemberService memberService;
    @Value("${spring.OAuth2.google.client-id}")
    private String GOOGLE_SNS_CLIENT_ID;
    @Value("${spring.OAuth2.google.client-secret}")
    private String GOOGLE_SNS_CLIENT_SECRET;
    @Value("${spring.OAuth2.kakao.client-id}")
    private String KAKAO_SNS_CLIENT_ID;
    @Value("${spring.OAuth2.kakao.client-secret}")
    private String KAKAO_SNS_CLIENT_SECRET;
    @Value("${spring.OAuth2.naver.client-id}")
    private String NAVER_SNS_CLIENT_ID;
    @Value("${spring.OAuth2.naver.client-secret}")
    private String NAVER_SNS_CLIENT_SECRET;
    @Value("${spring.OAuth2.apple.client-id}")
    private String APPLE_SNS_CLIENT_ID;
    @Value("${spring.OAuth2.apple.key-path}")
    private String APPLE_SNS_KEY_PATH;
    @Value("${spring.OAuth2.apple.auth-url}")
    private String APPLE_SNS_AUTH_URL;
    @Value("${spring.OAuth2.apple.key-id}")
    private String APPLE_SNS_KEY_ID;
    @Value("${spring.OAuth2.apple.team-id}")
    private String APPLE_SNS_TEAM_ID;
    @Value("${spring.OAuth2.apple.redirect-url}")
    private String APPLE_SNS_REDIRECT_URL;

    /***
     * 회원가입
     * 닉네임 입력후 확인 누를시 동작
     * 각 snsType에 맞춰 사용자 정보 가져오기 후 사용자 정보를 이용하여 회원생성
     * @param request
     * @return
     */
    @PostMapping("/api/v1/sign-up")
    public JSendResponse addMember(@RequestBody MemberDto.CreateMemberRequestV1 request) {
        try {
            String snsType = request.getSnsType();
            String uniqueId = null;
            String accessToken = null;
            String refreshToken = null;
            Long expiresIn = null;
            //snsType에 맞는 회원 데이터 가져오기
            if (snsType.equals("google")) {
                GoogleTokenResponse googleToken = SignInWithGoogle.getGoogleToken(GOOGLE_SNS_CLIENT_ID, GOOGLE_SNS_CLIENT_SECRET, request.getAuthCode()); //authCode로 act rft가져오기
                accessToken = googleToken.getAccessToken();
                refreshToken = googleToken.getRefreshToken();
                expiresIn = googleToken.getExpiresInSeconds();
                GoogleIdToken.Payload payload = SignInWithGoogle.getGoogleUserPayload(GOOGLE_SNS_CLIENT_ID, request.getIdToken());
                uniqueId = (String) payload.get("sub");
            } else if (snsType.equals("kakao")) {
                //refreshToken으로 accessToken, idToken가져오기
                String userInfoInRefreshToken = SignInWithKakao.renewKakaoRefreshToken(KAKAO_SNS_CLIENT_ID, KAKAO_SNS_CLIENT_SECRET, request.getRefreshToken());
                // responseBody에 있는 정보 읽기
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNodeRefreshToken = objectMapper.readTree(userInfoInRefreshToken);

                accessToken = jsonNodeRefreshToken.get("access_token").asText();
                expiresIn = jsonNodeRefreshToken.get("expires_in").asLong();
                refreshToken = request.getRefreshToken();
                //accessToken으로 유저정보가져오기
                String userInfo = SignInWithKakao.getUserInfoBody(accessToken);
                JsonNode jsonNodeUserInfo = objectMapper.readTree(userInfo);

                //식별자 정보
                uniqueId = jsonNodeUserInfo.get("id").asText();
            } else if (snsType.equals("naver")) {
                //refreshToken으로 accessToken, idToken가져오기
                String userInfoInRefreshToken = SignInWithNAVER.renewNaverRefreshToken(NAVER_SNS_CLIENT_ID, NAVER_SNS_CLIENT_SECRET, request.getRefreshToken());

                // responseBody에 있는 정보 읽기
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNodeRefreshToken = objectMapper.readTree(userInfoInRefreshToken);

                accessToken = jsonNodeRefreshToken.get("access_token").asText();
                expiresIn = jsonNodeRefreshToken.get("expires_in").asLong(); // 공문은 int로 리턴
                refreshToken = request.getRefreshToken();

                //accessToken으로 유저정보가져오기
                String userInfo = SignInWithNAVER.getUserInfoBody(accessToken);

                //식별자 정보
                JsonNode jsonNodeUserInfo = objectMapper.readTree(userInfo);
                uniqueId = jsonNodeUserInfo.get("response").get("id").asText();
            } else if (snsType.equals("appleAndroid") || snsType.equals("apple")) {
                if (request.getState() != null) {
                    //안드 apple 로그인
                    Member member = memberService.findOptionalMemberByState(request.getState()).orElseThrow(NullPointerException::new);
                    Member member1 = memberService.SignUpAppleAndroid(member, request.getMarketingConsent(), request.getNickname(), request.getFcmToken());

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("accessToken", member1.getAccessToken());
                    jsonObject.put("refreshToken", member1.getRefreshToken());
                    jsonObject.put("expiresIn", member1.getExpiresIn());

                    return new JSendResponse(HTTP_SUCCESS, null, jsonObject);
                } else {
                    String jwt = SigninWithAppleJWT.generateSignedJWT(
                            "K5RZZ64DS3",
                            "com.clonect.feeltalk"
                    );
                    //response가져오기
                    String authorizationCode = SigninWithAppleJWT.exchangeAuthorizationCodeIOS(jwt, request.getAuthorizationCode());

                    //response읽기
                    ObjectMapper mapper = new ObjectMapper();
                    MemberDto.AppleTokenResponse response = mapper.readValue(authorizationCode, MemberDto.AppleTokenResponse.class);

                    accessToken = response.getAccessToken();
                    expiresIn = response.getExpiresIn();
                    String idToken = response.getIdToken();
                    refreshToken = response.getRefreshToken();

                    //idToken에서 유저데이터 뽑아오기
                    JsonObject appleUserInfo = getAppleUserInfo(idToken);
                    JsonElement appleAlg = appleUserInfo.get("sub");
                    uniqueId = appleAlg.toString();
                }
            } else {
                return new JSendResponse(HTTP_FAIL, "회원가입 에러");
            }

            Optional<Member> isMember = memberService.findOptionalMemberByUniqueId(uniqueId);
            if (isMember.isPresent()) {
                return new JSendResponse(HTTP_FAIL, "이미 존재 하는 회원입니다.");
            }

            //회원생성
            Member signUpMember = memberService.SignUp(accessToken, refreshToken, uniqueId, expiresIn, request.getMarketingConsent(), request.getNickname(), request.getFcmToken(), snsType);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("accessToken", accessToken);
            jsonObject.put("refreshToken", refreshToken);
            jsonObject.put("expiresIn", expiresIn);
            return new JSendResponse(HTTP_SUCCESS, null, jsonObject);
        } catch (GeneralSecurityException e) {
            return new JSendResponse(HTTP_FAIL, e.toString());
        } catch (JsonMappingException e) {
            return new JSendResponse(HTTP_FAIL, e.toString());
        } catch (JsonProcessingException e) {
            return new JSendResponse(HTTP_FAIL, e.toString());
        } catch (IOException e) {
            return new JSendResponse(HTTP_FAIL, e.toString());
        } catch (JOSEException e) {
            return new JSendResponse(HTTP_FAIL, e.toString());
        }
    }

    /***
     * 안드로이드용 애플로그인 시 callback함수로 애플에서 "login/apple" url로 보내줌
     * code로 회원데이터 얻을수있음
     * 콜백으로온 state는 회원가입시 클라이언트에서 보내는 state코드와 비교하여 인증하는 용도로 사용
     * @param code
     * @param state
     * @throws Exception
     */
    @RequestMapping(value = "/login/apple")
    public void oauth_apple_v1(
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

            String accessToken = response.getAccessToken();
            Long expiresIn = response.getExpiresIn();
            String idToken = response.getIdToken();
            String refreshToken = response.getRefreshToken();
            String tokenType = response.getTokenType();

            //idToken에서 유저데이터 뽑아오기
            JsonObject appleUserInfo = getAppleUserInfo(idToken);

            JsonElement appleAlg = appleUserInfo.get("sub");
            JsonElement email = appleUserInfo.get("email");

            String userId = appleAlg.getAsString();
            Member member = memberService.findMemberByUniqueId(userId);
            if (member == null) {
                String loginState = memberService.callbackAppleAndroid(accessToken, refreshToken, userId, expiresIn, state, "apple");
            } else {
                member.setState(state);
                memberService.join(member);
            }
        } catch (JOSEException | JsonProcessingException e) {
            log.info("애플 회원가입/재로그인 실패" + e.getMessage());
            throw e;
        }
    }
}