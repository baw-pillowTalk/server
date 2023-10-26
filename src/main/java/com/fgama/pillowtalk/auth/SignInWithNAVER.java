package com.fgama.pillowtalk.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class SignInWithNAVER {


    /***
     * refresh Token 으로 accessToken갱신 하는 함수 , refresh토큰 기간이 얼마 남지 않았을땐 refreshToken도 같이 넘어옴
     * @param client_id
     * @param client_secret
     * @param refreshToken
     * @return
     * @throws RuntimeException
     */
    public static String renewNaverRefreshToken(String client_id, String client_secret, String refreshToken) throws RuntimeException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", client_id);
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        body.add("client_secret", client_secret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("https://nid.naver.com/oauth2.0/token", request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("naver refresh Token갱신 실패 (검증실패): " + response.getStatusCodeValue());
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

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                kakaoUserInfoRequest,
                String.class
        );
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("naver accessToken 검증실패 : " + response.getStatusCodeValue());
        }

    }

    /***
     * accessToken 검증함수
     * @param accessToken
     * @return
     */
    public static Boolean validAccessToken(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            // HTTP 요청 보내기
            HttpEntity<MultiValueMap<String, String>> naverUserInfoRequest = new HttpEntity<>(headers);
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> response = rt.exchange(
                    "https://openapi.naver.com/v1/nid/me",
                    HttpMethod.GET,
                    naverUserInfoRequest,
                    String.class
            );

            return response.getStatusCode().equals(HttpStatus.OK);
        } catch (RuntimeException e) {
            return false;
        }
    }
}