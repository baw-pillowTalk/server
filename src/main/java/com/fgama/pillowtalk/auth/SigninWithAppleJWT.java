package com.fgama.pillowtalk.auth;

import com.google.gson.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

@Slf4j
public class SigninWithAppleJWT {

    /***
     * 애플용 client_secret 만드는 함수
     * @param teamId
     * @param clientId
     * @return
     * @throws JOSEException
     */
    public static String generateSignedJWT(String teamId, String clientId) throws JOSEException {

        // Create the JWT header with ES256 algorithm and key ID
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID("XDVLLW9Q3T") // Replace with your 10-character key ID
                .build();

        // Create the JWT payload with issuer, subject, expiration time, audience, and client ID
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(teamId) // Replace with your 10-character Team ID
                .subject(clientId) // Replace with your client ID
                .expirationTime(Date.from(Instant.now().plusSeconds(3600))) // Set to expire in 1 hour
                .audience("https://appleid.apple.com")
                .issueTime(Date.from(Instant.now()))
                .build();

        // Create the JWS object and sign it with the private key
        JWSObject jwsObject = new JWSObject(header, new Payload(claims.toJSONObject()));
        Security.addProvider(new BouncyCastleProvider());

        // Auth_key.p8 파일 읽어들이기
        File privateKeyFile = new File("AuthKey_XDVLLW9Q3T.p8");
        PemReader reader = null;
        try {
            reader = new PemReader(new InputStreamReader(new FileInputStream(privateKeyFile)));
            PemObject pemObject = reader.readPemObject();
            byte[] privateKeyBytes = pemObject.getContent();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privateKeyBytes);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            ECPrivateKey privateKey = (ECPrivateKey) converter.getPrivateKey(pkInfo);
            //서명
            JWSSigner signer = new ECDSASigner(privateKey);
            jwsObject.sign(signer);

            // Serialize the signed JWT to a string
            String signedJWT = jwsObject.serialize();
            return signedJWT;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /***
     * authorizationCode검증 후 사용자 데이터 반환 함수
     * @param clientSecret
     * @param code
     * @return
     */
    public static String exchangeAuthorizationCodeIOS(String clientSecret, String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", "com.clonect.feeltalk");
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", "https://clonect.net/login/apple");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("https://appleid.apple.com/auth/token", request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to exchange authorization code for access token: " + response.getStatusCodeValue());
        }
    }

    public static String exchangeAuthorizationCode(String clientSecret, String code) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", "clonect.com.feeltalk");
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", "https://clonect.net/login/apple");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("https://appleid.apple.com/auth/token", request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to exchange authorization code for access token: " + response.getStatusCodeValue());
        }
    }

    /***
     * 토큰 검증 및 생성 코드
     * @param clientSecret
     * @param refreshToken
     * @return
     */
    public static String validAccessToken(String clientSecret, String refreshToken) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", "clonect.com.feeltalk");
        body.add("client_secret", clientSecret);
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("https://appleid.apple.com/auth/token", request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to exchange authorization code for access token: " + response.getStatusCodeValue());
        }
    }

    /***
     * idToken에 있는 알고리즘 값이 애플에서 제공하는 공개키 3개중 맞는지 판단 후 통과 하면 사용자 데이터 반환 함수
     * @param idToken
     * @return
     */
    public static JsonObject getAppleUserInfo(String idToken) {
        StringBuffer result = new StringBuffer();

        try {
            URL url = new URL("https://appleid.apple.com/auth/keys");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";

            while ((line = br.readLine()) != null) {
                result.append(line);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonParser parser = new JsonParser();
        JsonObject keys = (JsonObject) parser.parse(result.toString());
        JsonArray keyArray = (JsonArray) keys.get("keys");


        //클라이언트로부터 가져온 identity token String decode
        String[] decodeArray = idToken.split("\\.");
        String header = new String(Base64.getDecoder().decode(decodeArray[0]));

        //apple에서 제공해주는 kid값과 일치하는지 알기 위해
        JsonElement kid = ((JsonObject) parser.parse(header)).get("kid");
        JsonElement alg = ((JsonObject) parser.parse(header)).get("alg");

        //써야하는 Element (kid, alg 일치하는 element)
        JsonObject avaliableObject = null;
        for (int i = 0; i < keyArray.size(); i++) {
            JsonObject appleObject = (JsonObject) keyArray.get(i);
            JsonElement appleKid = appleObject.get("kid");
            JsonElement appleAlg = appleObject.get("alg");

            if (Objects.equals(appleKid, kid) && Objects.equals(appleAlg, alg)) {
                avaliableObject = appleObject;
                break;
            }
        }

        //일치하는 공개키 없음
        PublicKey publicKey = getPublicKey(avaliableObject);

        //--> 여기까지 검증

        Claims userInfo = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(idToken).getBody();
        JsonObject userInfoObject = (JsonObject) parser.parse(new Gson().toJson(userInfo));
        return userInfoObject;

    }

    /***
     * 공개키 얻는 함수수     * @param object
     * @return
     */
    public static PublicKey getPublicKey(JsonObject object) {
        String nStr = object.get("n").toString();
        String eStr = object.get("e").toString();

        byte[] nBytes = Base64.getUrlDecoder().decode(nStr.substring(1, nStr.length() - 1));
        byte[] eBytes = Base64.getUrlDecoder().decode(eStr.substring(1, eStr.length() - 1));

        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);

        try {
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return publicKey;
        } catch (Exception exception) {
            log.info("공개키 없음");
        }
        return null;
    }
}