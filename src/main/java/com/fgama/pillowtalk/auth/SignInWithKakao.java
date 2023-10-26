package com.fgama.pillowtalk.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class SignInWithKakao {

    /***
     * refresh Token 으로 accessToken갱신 하는 함수 , refresh토큰 기간이 얼마 남지 않았을땐 refreshToken도 같이 넘어옴
     * @param client_id
     * @param client_secret
     * @param refreshToken
     * @return
     * @throws RuntimeException
     */
    public static String renewKakaoRefreshToken(String client_id, String client_secret, String refreshToken) throws RuntimeException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", client_id);
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        body.add("client_secret", client_secret);
        /***
         * 토큰 발급 시, 보안을 강화하기 위해 추가 확인하는 코드
         * [내 애플리케이션] > [보안]에서 설정 가능
         * ON 상태인 경우 필수 설정해야 함
         */
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("https://kauth.kakao.com/oauth/token", request, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("kakao refresh Token갱신 실패 (검증실패): " + response.getStatusCodeValue());
        }
    }

    /***
     * accessToken으로 사용자 정보 가져오는 함수
     * 현제 사용 x
     * @param accessToken
     * @return
     */
    public static String getUserInfoBody(String accessToken) throws RuntimeException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("kakao accessToken 검증실패 : " + response.getStatusCodeValue());
        }
    }

    /***
     * 카카오톡 accessToken 검증 함수
     * @param accessToken
     * @return
     */
    public static Boolean validAccessToken(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            // HTTP 요청 보내기
            HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> response = rt.exchange(
                    "https://kapi.kakao.com/v1/user/access_token_info",
                    HttpMethod.GET,
                    kakaoUserInfoRequest,
                    String.class
            );
            return response.getStatusCode().equals(HttpStatus.OK);
        } catch (RuntimeException e) {
            return false;
        }
    }
}