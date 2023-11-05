//package com.fgama.pillowtalk.api;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fgama.pillowtalk.auth.SigninWithAppleJWT;
//import com.fgama.pillowtalk.domain.Member;
//import com.fgama.pillowtalk.dto.JSendResponse;
//import com.fgama.pillowtalk.dto.MemberDto;
//import com.fgama.pillowtalk.service.MemberService;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.nimbusds.jose.JOSEException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.*;
//
//import static com.fgama.pillowtalk.auth.SigninWithAppleJWT.getAppleUserInfo;
//
//@RestController
//@Slf4j
//@RequiredArgsConstructor
//public class SignUpApi {
//    public static final String HTTP_SUCCESS = "success";
//    public static final String HTTP_FAIL = "fail";
//    private final MemberService memberService;
//    @Value("${spring.OAuth2.google.client-id}")
//    private String GOOGLE_SNS_CLIENT_ID;
//    @Value("${spring.OAuth2.google.client-secret}")
//    private String GOOGLE_SNS_CLIENT_SECRET;
//    @Value("${spring.OAuth2.kakao.client-id}")
//    private String KAKAO_SNS_CLIENT_ID;
//    @Value("${spring.OAuth2.kakao.client-secret}")
//    private String KAKAO_SNS_CLIENT_SECRET;
//    @Value("${spring.OAuth2.naver.client-id}")
//    private String NAVER_SNS_CLIENT_ID;
//    @Value("${spring.OAuth2.naver.client-secret}")
//    private String NAVER_SNS_CLIENT_SECRET;
//    @Value("${spring.OAuth2.apple.client-id}")
//    private String APPLE_SNS_CLIENT_ID;
//    @Value("${spring.OAuth2.apple.key-path}")
//    private String APPLE_SNS_KEY_PATH;
//    @Value("${spring.OAuth2.apple.auth-url}")
//    private String APPLE_SNS_AUTH_URL;
//    @Value("${spring.OAuth2.apple.key-id}")
//    private String APPLE_SNS_KEY_ID;
//    @Value("${spring.OAuth2.apple.team-id}")
//    private String APPLE_SNS_TEAM_ID;
//    @Value("${spring.OAuth2.apple.redirect-url}")
//    private String APPLE_SNS_REDIRECT_URL;
//
//    /***
//     * 회원가입
//     * 닉네임 입력후 확인 누를시 동작
//     * 각 snsType에 맞춰 사용자 정보 가져오기 후 사용자 정보를 이용하여 회원 생성
//     */
//    @PostMapping("/api/v1/sign-up")
//    public JSendResponse addMember(
//            @RequestBody MemberDto.CreateMemberRequestV1 request
//    ) {
//
//    }
//
//    /***
//     * 안드로이드용 애플로그인 시 callback함수로 애플에서 "login/apple" url로 보내줌
//     * code로 회원데이터 얻을수있음
//     * 콜백으로온 state는 회원가입시 클라이언트에서 보내는 state코드와 비교하여 인증하는 용도로 사용
//     */
//    @RequestMapping(value = "/login/apple")
//    public void oauth_apple_v1(
//            @RequestParam(value = "code", required = false) String code,
//            @RequestParam(value = "state", required = false) String state
//    ) throws Exception { // android에서 애플가입 웹뷰
//        try {
//            //clientSecret만들기
//            String jwt = SigninWithAppleJWT.generateSignedJWT(
//                    "K5RZZ64DS3",
//                    "clonect.com.feeltalk"
//            );
//            //정보가져오기
//            String authorizationCode = SigninWithAppleJWT.exchangeAuthorizationCode(jwt, code);
//
//            //정보추출
//            ObjectMapper mapper = new ObjectMapper();
//            MemberDto.AppleTokenResponse response = mapper.readValue(authorizationCode, MemberDto.AppleTokenResponse.class);
//
//            String accessToken = response.getAccessToken();
//            Long expiresIn = response.getExpiresIn();
//            String idToken = response.getIdToken();
//            String refreshToken = response.getRefreshToken();
//            String tokenType = response.getTokenType();
//
//            //idToken에서 유저데이터 뽑아오기
//            JsonObject appleUserInfo = getAppleUserInfo(idToken);
//
//            JsonElement appleAlg = appleUserInfo.get("sub");
//            JsonElement email = appleUserInfo.get("email");
//
//            String userId = appleAlg.getAsString();
//            Member member = memberService.findMemberByOauthIdAndSnsType(userId);
//            if (member == null) {
//                String loginState = memberService.callbackAppleAndroid(accessToken, refreshToken, userId, expiresIn, state, "apple");
//            } else {
//                member.setState(state);
//                memberService.join(member);
//            }
//        } catch (JOSEException | JsonProcessingException e) {
//            log.info("애플 회원가입/재로그인 실패" + e.getMessage());
//            throw e;
//        }
//    }
//}