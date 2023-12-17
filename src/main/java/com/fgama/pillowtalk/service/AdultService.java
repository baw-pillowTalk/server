package com.fgama.pillowtalk.service;


import com.dreamsecurity.json.JSONException;
import com.dreamsecurity.mobileOK.MobileOKException;
import com.dreamsecurity.mobileOK.mobileOKKeyManager;
import com.fgama.pillowtalk.dto.adult.AdultAuthenticationTokenDto.AdultAuthenticationRequestDto;
import com.fgama.pillowtalk.dto.adult.AdultAuthenticationVerificationRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Service
public class AdultService {
    private final static String PREFIX_ID = "MOK"; // 서비스 아이디 Prefix
    private final static String USAGE_CODE = "01006"; // 성인인증 코드
    private final static String SERVICE_TYPE = "telcoAuth"; // 서비스 타입
    private final static String RET_TRANSFER_TYPE = "MOKResult"; // 요청 대상의 결과 전송 타입
    private static final String SERVICE_DOMAIN = "http://3.38.37.33:8080";
    @Value("${adult.token.url}")
    private String tokenUrl; // 1. 본인확인 토큰 요청 url
    @Value("${adult.auth.url}")
    private String authUrl; // 2. 본인확인 인증 요청 url
    @Value("${adult.reauth.url}")
    private String reAuthUrl; // 3. 본인확인 재인증 요청 url
    @Value("${adult.keyfile.password}")
    private String keyFilePassword; // 4. 키파일 비밀번호
    @Value("${adult.keyfile.location}")
    private String keyFileLocation; // 5. 키파일 위치
    @Value("${adult.verification.url}")
    private String verificationUrl; // 6. 본인확인 인증 검증 url

    /* 1-0. 본인확인 인증 요청 API */
    @Transactional
    public com.dreamsecurity.json.JSONObject adultAuthenticationRequest(AdultAuthenticationRequestDto request) throws MobileOKException {
        mobileOKKeyManager mobileOK = null;
        try {
            // 본인확인 키파일을 통한 비밀키
            mobileOK = new mobileOKKeyManager();
            mobileOK.keyInit(keyFileLocation, keyFilePassword);
            mobileOK.setSiteUrl(SERVICE_DOMAIN); // pillow talk 도메인
        } catch (MobileOKException e) {
            throw new MobileOKException(e.getErrorCode() + "|" + e.getMessage());
        }
        // 1. 본인확인 토큰 요청을 위한 문자열 데이터 설정
        String mokGetTokenResponseJsonString = this.mobileOkApiGetToken(mobileOK);

        // 2. 본인확인 토큰 요청
        String mokGetTokenResponse = this.sendTokenRequest(tokenUrl, mokGetTokenResponseJsonString);


        assert mokGetTokenResponse != null;
        // JSONObject 객체로 변환
        /**
         * - encryptMokToken,
         * - publikKey,
         * - resultCode,
         * - resultMsg
         **/
        com.dreamsecurity.json.JSONObject mokGetTokenResponseJson = new com.dreamsecurity.json.JSONObject(mokGetTokenResponse);

        if (!Objects.equals(mokGetTokenResponseJson.getString("resultCode"), "2000")) {
            throw new RuntimeException("본인확인 거래 토큰 요청 API 가 정상저긍로 처리되지 않았습니다.");
        }

        String token = mokGetTokenResponseJson.getString("encryptMOKToken"); // 토큰
        String publicKey = mokGetTokenResponseJson.getString("publicKey"); // 암호화 용도의 공개키
        String siteUrl = SERVICE_DOMAIN;

        // ---------------------------------------------------------------------------------------- //


        String mobilOkApiAuthResponse = this.mobileOkApiAuthRequest(token, publicKey, siteUrl, mobileOK, request);
        return null;
    }

    private String mobileOkApiAuthRequest(String token, String publicKey, String siteUrl,
                                          mobileOKKeyManager mobileOK, AdultAuthenticationRequestDto request
    ) throws MobileOKException {
        String mokAuthRequestJsonString = mokAuthRequestToJsonString(
                mobileOK,
                publicKey,
                token,
                siteUrl,
                request.getProviderId(),
                request.getReqAuthType(),
                USAGE_CODE,
                SERVICE_TYPE,
                request.getUserName(),
                request.getUserPhone(),
                RET_TRANSFER_TYPE,
                null,
                request.getUserBirthday(),
                request.getUserGender(),
                request.getUserNation(),
                null,
                null);

        String mokAuthResponseJsonString = this.sendRequestAuth(authUrl, mokAuthRequestJsonString);

        com.dreamsecurity.json.JSONObject mokAuthResponseJson = new com.dreamsecurity.json.JSONObject(Objects.requireNonNull(mokAuthResponseJsonString));

        if (!"2000".equals(mokAuthResponseJson.getString("resultCode"))) {
            return mokAuthResponseJson.toString();
        }

        return null;
    }


    /* 1-1. 본인확인 토큰 받기 요청 데이터 생성 */
    private String mobileOkApiGetToken(mobileOKKeyManager mobileOK) throws MobileOKException {
        // 이용기간 거래 ID 생성
        String sampleClientTxId = PREFIX_ID + UUID.randomUUID().toString().replaceAll("-", "");

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String reqClientInfo = sampleClientTxId + "|" + formatter.format(cal.getTime());

        // 이용기간 거래 ID 비밀키로 암호화
        String encryptReqClientInfo = mobileOK.RSAEncrypt(reqClientInfo);

        JSONObject mokGetTokenRequestJson = new JSONObject();
        mokGetTokenRequestJson.put("serviceId", mobileOK.getServiceId());
        mokGetTokenRequestJson.put("encryptReqClientInfo", encryptReqClientInfo);
        mokGetTokenRequestJson.put("siteUrl", mobileOK.getSiteUrl());
        return mokGetTokenRequestJson.toString();
    }


    /* 2-1. 본인확인 토큰 요청 */
    private String sendTokenRequest(String tokenUrl, String json) throws MobileOKException {
        HttpURLConnection connection = null;
        DataOutputStream dataOutputStream = null;
        BufferedReader bufferedReader = null;
        StringBuffer responseData = null;

        try {
            URL url = new URL(tokenUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setDoOutput(true);

            dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(json);

            bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );
            responseData = new StringBuffer();
            String info;
            while ((info = bufferedReader.readLine()) != null) {
                responseData.append(info);
            }
            return responseData.toString();
        } catch (FileNotFoundException e) {
            throw new MobileOKException("MOK_GET_TOKEN_URL을 확인해 주세요.");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }

                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }

                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /* 3.1 본인확인-API 인증요청 데이터 설정 */

    private String mokAuthRequestToJsonString(
            mobileOKKeyManager mobileOK
            , String publicKey
            , String encryptMOKToken
            , String siteUrl
            , String providerId
            , String reqAuthType
            , String usageCode
            , String serviceType
            , String userName
            , String userPhoneNum
            , String retTransferType
            , String arsCode
            , String userBirthday
            , String userGender
            , String userNation
            , String sendMsg
            , String replyNumber
    ) throws MobileOKException {

        JSONObject MOKAuthInfoJson = new JSONObject();
        MOKAuthInfoJson.put("providerId", providerId);
        MOKAuthInfoJson.put("reqAuthType", reqAuthType);
        MOKAuthInfoJson.put("usageCode", usageCode);
        MOKAuthInfoJson.put("serviceType", serviceType);
        MOKAuthInfoJson.put("userName", userName);
        MOKAuthInfoJson.put("userPhone", userPhoneNum);
        MOKAuthInfoJson.put("retTransferType", retTransferType);
        MOKAuthInfoJson.put("userBirthDay", userBirthday);
        MOKAuthInfoJson.put("userGender", userGender);
        MOKAuthInfoJson.put("userNation", userNation);
        MOKAuthInfoJson.put("sendMsg", sendMsg);
        MOKAuthInfoJson.put("replyNumber", replyNumber);


        // 유저 정보 암호화
        String encMOKAuthInfo = mobileOK.RSAServerEncrypt(publicKey, MOKAuthInfoJson.toString());

        JSONObject MOKAuthRequestJson = new JSONObject();
        MOKAuthRequestJson.put("siteUrl", siteUrl);
        MOKAuthRequestJson.put("encryptMOKToken", encryptMOKToken);
        MOKAuthRequestJson.put("encryptMOKAuthInfo", encMOKAuthInfo);

        return MOKAuthRequestJson.toString();
    }


    // 본인확인 인증 요청
    private String sendRequestAuth(String dest, String jsonData) throws MobileOKException {
        HttpURLConnection connection = null;
        DataOutputStream dataOutputStream = null;
        BufferedReader bufferedReader = null;
        StringBuffer responseData = null;

        try {
            URL url = new URL(dest);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setDoOutput(true);

            dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.write(jsonData.getBytes(StandardCharsets.UTF_8));

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            responseData = new StringBuffer();
            String info;
            while ((info = bufferedReader.readLine()) != null) {
                responseData.append(info);
            }

            return responseData.toString();
        } catch (FileNotFoundException e) {
            throw new MobileOKException("MOK_REQUEST_URL 또는 MOK_RESEND_URL을 확인해주세요.");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }

                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }

                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    private String setErrorMsg(String errorMsg) {
        JSONObject errorJson = new JSONObject();
        errorJson.put("resultMsg", errorMsg);

        return errorJson.toString();
    }

    /* 2-0 본인확인 인증 검증 요청 */
    @Transactional
    public void adultAuthenticationVerificationRequest(AdultAuthenticationVerificationRequestDto request) throws MobileOKException {
        mobileOKKeyManager mobileOK = null;
        try {
            // 본인확인 키파일을 통한 비밀키
            mobileOK = new mobileOKKeyManager();
            mobileOK.keyInit(keyFileLocation, keyFilePassword);
            mobileOK.setSiteUrl(SERVICE_DOMAIN); // pillow talk 도메인
        } catch (MobileOKException e) {
            throw new MobileOKException(e.getErrorCode() + "|" + e.getMessage());
        }
        // 1. 본인확인 토큰 요청을 위한 문자열 데이터 설정
        String mokGetTokenResponseJsonString = this.mobileOkApiGetToken(mobileOK);

        // 2. 본인확인 토큰 요청
        String mokGetTokenResponse = this.sendTokenRequest(tokenUrl, mokGetTokenResponseJsonString);

        com.dreamsecurity.json.JSONObject mokGetTokenResponseJson = new com.dreamsecurity.json.JSONObject(Objects.requireNonNull(mokGetTokenResponse));

        if (!Objects.equals(mokGetTokenResponseJson.getString("resultCode"), "2000")) {
            throw new RuntimeException("본인확인 거래 토큰 요청 API 가 정상저긍로 처리되지 않았습니다.");
        }

        String token = mokGetTokenResponseJson.getString("encryptMOKToken"); // 토큰
        String publicKey = mokGetTokenResponseJson.getString("publicKey"); // 암호화 용도의 공개키
        String siteUrl = SERVICE_DOMAIN;

        // 3. 본인확인 검증 요청
        String result = this.mobileOkApiResult(mobileOK, token, publicKey, siteUrl, request);

    }

    private String mobileOkApiResult(mobileOKKeyManager mobileOK, String token,
                                     String publicKey, String siteUrl,
                                     AdultAuthenticationVerificationRequestDto request) throws MobileOKException {

        String requestToJsonString = this.mokConfirmRequestToJsonString(mobileOK, token, publicKey, request.getAuthNumber());
        // 검증 요청
        String mokConfirmResponse = this.sendVerificationRequest(requestToJsonString);

        com.dreamsecurity.json.JSONObject decryptResultJson = null;

        try {
            com.dreamsecurity.json.JSONObject mokConfirmResponseJson
                    = new com.dreamsecurity.json.JSONObject(Objects.requireNonNull(mokConfirmResponse));

            if (!"2000".equals(mokConfirmResponseJson.getString("resultCode"))) {
                String encryptMokToken = mokConfirmResponseJson.optString("encryptMOKToken", null);
                if (encryptMokToken != null) {
                    encryptMokToken = URLEncoder.encode(encryptMokToken, StandardCharsets.UTF_8);

                    com.dreamsecurity.json.JSONObject retryDataJson = new com.dreamsecurity.json.JSONObject();
                    retryDataJson.put("publicKey", publicKey);
                    retryDataJson.put("encryptMOKToken", encryptMokToken);
                    retryDataJson.put("resultCode", mokConfirmResponseJson.getString("resultCode"));
                    retryDataJson.put("resultMsg", mokConfirmResponseJson.getString("resultMsg"));
                    return retryDataJson.toString();
                } else {
                    return mokConfirmResponse;
                }
            }

            String encryptMokResult = mokConfirmResponseJson.getString("encryptMOKResult");
            decryptResultJson = new com.dreamsecurity.json.JSONObject(mobileOK.getResultJSON(encryptMokResult));
        } catch (MobileOKException exception) {
            return setErrorMsg(exception.getMessage());
        }

        com.dreamsecurity.json.JSONObject resultJsonObject = new com.dreamsecurity.json.JSONObject();
        resultJsonObject.put("resultCode", "2000");
        resultJsonObject.put("resultMsg", "성공");
        resultJsonObject.put("userName", decryptResultJson.optString("userName", null));
        resultJsonObject.put("userBirthday", decryptResultJson.optString("userBirthday", null));

        return resultJsonObject.toString();
    }


    private String sendVerificationRequest(String jsonData) throws MobileOKException {
        HttpURLConnection connection = null;
        DataOutputStream dataOutputStream = null;
        BufferedReader bufferedReader = null;

        try {
            URL url = new URL(verificationUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setDoOutput(true);

            dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.write(jsonData.getBytes(StandardCharsets.UTF_8));

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer responseData = new StringBuffer();
            String info;
            while ((info = bufferedReader.readLine()) != null) {
                responseData.append(info);
            }
            return responseData.toString();
        } catch (FileNotFoundException e) {
            throw new MobileOKException("-5|MOK_CONFIRM_URL을 확인해주세요.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }

                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }

                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String mokConfirmRequestToJsonString(
            mobileOKKeyManager mobileOKKeyManager,
            String encryptMokToken,
            String publicKey,
            String authNumber
    ) throws MobileOKException {

        JSONObject MOKconfirmRequestJson = new JSONObject();
        MOKconfirmRequestJson.put("encryptMOKToken", encryptMokToken);

        if (null != authNumber
                && !authNumber.isEmpty()) {
            JSONObject authNumberJson = new JSONObject();
            authNumberJson.put("authNumber", authNumber);

            String encAuthNumber = mobileOKKeyManager.RSAServerEncrypt(publicKey, authNumberJson.toString());

            MOKconfirmRequestJson.put("encryptMOKVerifyInfo", encAuthNumber);
        }
        return MOKconfirmRequestJson.toString();
    }
}
