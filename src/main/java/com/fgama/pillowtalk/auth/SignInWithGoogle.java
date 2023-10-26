package com.fgama.pillowtalk.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
public class SignInWithGoogle {
    /***
     * idToken 읽고 검증후 payload 리턴
     * @param : idToken
     * @return :GoogleIdToken.Payload
     */
    //
    public static GoogleIdToken.Payload getGoogleUserPayload(String clientId, String idToken) throws GeneralSecurityException, IOException {
        //idTOken검증
        GoogleIdToken googleIdToken = getGoogleIdToken(clientId, idToken);

        if (googleIdToken == null) {
            throw new RuntimeException("Can't read payload because googleIdToken is null");
        }
        return googleIdToken.getPayload();
    }

    /***
     * idToken검증 함수
     * @param :
     * @return :
     */
    private static GoogleIdToken getGoogleIdToken(String clientId, String idToken) throws
            GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = createVerifier(clientId);
        return verifier.verify(idToken);
    }

    /***
     * verifier만들기
     * @param :
     * @return :
     */
    private static GoogleIdTokenVerifier createVerifier(String client_id) {
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();
        //토큰검증
        return new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(client_id))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();
    }

    /***
     * accessToken 검증 함수
     * @param accessToken
     * @return
     * @throws IOException
     * @throws RuntimeException
     */
    public static Boolean validAccessToken(String accessToken) throws IOException, RuntimeException {
        URL url = new URL("https://oauth2.googleapis.com/tokeninfo?access_token=" + accessToken);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return true;
        } else {
            return false;
        }
    }

    /***
     * refreshToken 으로 accessToken갱신하는 함
     * @param clientId
     * @param clientSecret
     * @param refreshToken
     * @return
     * @throws RuntimeException
     */
    public static String getAccessTokenResponseWithRefreshToken(String clientId, String clientSecret, String refreshToken) throws RuntimeException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("https://oauth2.googleapis.com/token", request, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("refreshToken(" + refreshToken + ") 로 accessToken 갱신 실패 (google): " + response.getStatusCodeValue());
        }
    }

    /***
     * authCode검증 후 accessToken 받아오기
     * @param : authCode
     * @return : accessToken refreshToken scope expire_in token_type
     */
    //
    public static GoogleTokenResponse getGoogleToken(String clientId, String clientSecret, String authCode) throws IOException {
        return new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://oauth2.googleapis.com/token",
                clientId,
                clientSecret,
                authCode,
                "")
                .execute();
    }
}