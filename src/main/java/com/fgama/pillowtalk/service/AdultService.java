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

    @Value("${adult.token.url}")
    private String tokenUrl; // 본인확인 토큰 요청 url

    @Value("${adult.auth.url}")
    private String authUrl; // 본인확인 인증 요청 url

    @Value("${adult.reauth.url}")
    private String reAuthUrl; // 본인확인 재인증 요청 url

    @Value("${adult.keyfile.password}")
    private String keyFilePassword; // 키파일 비밀번호

    @Value("${adult.verification.url}")
    private String verificationUrl; // 본인확인 인증 검증 url

    /* 1-0. 본인확인 인증 요청 API */
    @Transactional
    public com.dreamsecurity.json.JSONObject adultAuthenticationRequest(AdultAuthenticationRequestDto request) throws MobileOKException {
        mobileOKKeyManager mobileOK = null;
        try {
            // 본인확인 키파일을 통한 비밀키
            mobileOK = new mobileOKKeyManager();
            mobileOK.keyInit(" /Users/DongKuen/Desktop/ForCoding/필로우톡/mok_keyInfo.dat", keyFilePassword);
            mobileOK.setSiteUrl("www.mobile-ok.com"); // pillow talk 도메인
        } catch (MobileOKException e) {
            throw new MobileOKException(e.getErrorCode() + "|" + e.getMessage());
        }
        // 1. 본인확인 토큰 요청을 위한 데이터 설정
        String mokGetTokenResponseJsonString = this.mobileOkApiGetToken(mobileOK);

        /**
         * - encryptMOKToken : 거래 토큰
         * - publicKey : 공개키
         * - resultCode : 응답 결과 코드
         * - resultMsg : 에러인 경우 에러 메세지
         **/
        // 2. 본인확인 토큰 요청
        String mokGetTokenResponse = this.sendRequestToken(tokenUrl, mokGetTokenResponseJsonString);


        assert mokGetTokenResponse != null;
        // JSONObject 객체로 변환
        com.dreamsecurity.json.JSONObject mokGetTokenResponseJson = new com.dreamsecurity.json.JSONObject(mokGetTokenResponse);

        String token = mokGetTokenResponseJson.getString("encryptMOKToken"); // 토큰
        String publicKey = mokGetTokenResponseJson.getString("publicKey"); // 암호화 용도의 공개키

        // ---------------------------------------------------------------------------------------- //

        // 3. 본인확인 인증 요청을 위한 데이터 설정
        String mokAuthRequestJsonString = this.mokAuthRequestToJsonString(
                mobileOK,
                publicKey,
                token,
                mobileOK.getSiteUrl(),
                request.getProviderId(),
                request.getReqAuthType(),
                USAGE_CODE,
                SERVICE_TYPE,
                request.getUserName(),
                request.getUserPhone(),
                RET_TRANSFER_TYPE,
                "",
                request.getUserBirthday(),
                request.getUserGender(),
                String.valueOf(request.getUserNation()),
                null,
                null);

        // 4. 본인확인 인증 요청
        String mokAuthResponseJsonString = this.sendRequestAuth(authUrl, mokAuthRequestJsonString);
        return new com.dreamsecurity.json.JSONObject(Objects.requireNonNull(mokAuthResponseJsonString));
    }


    /* 1-1. 본인확인 토큰 받기 요청 데이터 생성 */
    private String mobileOkApiGetToken(mobileOKKeyManager mobileOK) throws MobileOKException {
        String sampleClientTxId = PREFIX_ID + UUID.randomUUID().toString().replaceAll("-", "");

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String reqClientInfo = sampleClientTxId + "|" + formatter.format(cal.getTime());
        String encryptReqClientInfo = mobileOK.RSAEncrypt(reqClientInfo); // 거래코드 비밀키로 암호화

        JSONObject mokGetTokenRequestJson = new JSONObject();
        mokGetTokenRequestJson.put("serviceId", mobileOK.getServiceId());
        mokGetTokenRequestJson.put("encryptReqClientInfo", encryptReqClientInfo);
        mokGetTokenRequestJson.put("siteUrl", mobileOK.getSiteUrl());
        return mokGetTokenRequestJson.toString();
    }


    /* 2-1. 본인확인 토큰 요청 */
    private String sendRequestToken(String tokenUrl, String json) throws MobileOKException {
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
            , String replyNumber) throws MobileOKException {

        JSONObject MOKAuthInfoJson = new JSONObject();
        MOKAuthInfoJson.put("providerId", providerId);
        MOKAuthInfoJson.put("reqAuthType", reqAuthType);
        MOKAuthInfoJson.put("usageCode", usageCode);
        MOKAuthInfoJson.put("serviceType", serviceType);
        MOKAuthInfoJson.put("userName", userName);
        MOKAuthInfoJson.put("userPhone", userPhoneNum);
        MOKAuthInfoJson.put("retTransferType", retTransferType);
        if ("telcoAuth-ARSAuth".equals(serviceType)) {
            MOKAuthInfoJson.put("arsCode", arsCode);
            MOKAuthInfoJson.put("userBirthday", userBirthday);
            MOKAuthInfoJson.put("userGender", userGender);
            MOKAuthInfoJson.put("userNation", userNation);
            if (!"".equals(sendMsg)) {
                MOKAuthInfoJson.put("sendMsg", sendMsg);
            }
            if (!"".equals(replyNumber)) {
                MOKAuthInfoJson.put("replyNumber", replyNumber);
            }
        } else if (serviceType.indexOf("telcoAuth") != -1
                && "SMS".equals(reqAuthType)
                || "LMS".equals(reqAuthType)) {
            MOKAuthInfoJson.put("userBirthday", userBirthday);
            MOKAuthInfoJson.put("userGender", userGender);
            MOKAuthInfoJson.put("userNation", userNation);
            if (!"".equals(sendMsg)) {
                MOKAuthInfoJson.put("sendMsg", sendMsg);
            }
            if (!"".equals(replyNumber)) {
                MOKAuthInfoJson.put("replyNumber", replyNumber);
            }
        } else if ("ARSAuth".equals(serviceType)) {
            MOKAuthInfoJson.put("arsCode", arsCode);
            if (!"".equals(sendMsg)) {
                MOKAuthInfoJson.put("sendMsg", sendMsg);
            }
            if (!"".equals(replyNumber)) {
                MOKAuthInfoJson.put("replyNumber", replyNumber);
            }
        } else if ("SMSAuth".equals(serviceType)) {
            if (!"".equals(sendMsg)) {
                MOKAuthInfoJson.put("sendMsg", sendMsg);
            }
            if (!"".equals(replyNumber)) {
                MOKAuthInfoJson.put("replyNumber", replyNumber);
            }
        }

        // 유저 정보 암호화
        String encMOKAuthInfo = mobileOK.RSAServerEncrypt(publicKey, MOKAuthInfoJson.toString());

        JSONObject MOKAuthRequestJson = new JSONObject();
        MOKAuthRequestJson.put("siteUrl", siteUrl);
        MOKAuthRequestJson.put("encryptMOKToken", encryptMOKToken);
        MOKAuthRequestJson.put("encryptMOKAuthInfo", encMOKAuthInfo);

        return MOKAuthRequestJson.toString();
    }

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
            mobileOK.keyInit(" /Users/DongKuen/Desktop/ForCoding/필로우톡/mok_keyInfo.dat", keyFilePassword);
            mobileOK.setSiteUrl("www.mobile-ok.com"); // pillow talk 도메인
        } catch (MobileOKException e) {
            throw new MobileOKException(e.getErrorCode() + "|" + e.getMessage());
        }
        // 1. 본인확인 토큰 요청을 위한 데이터 설정
        String mokGetTokenResponseJsonString = this.mobileOkApiGetToken(mobileOK);

        /**
         * - encryptMOKToken : 거래 토큰
         * - publicKey : 공개키
         * - resultCode : 응답 결과 코드
         * - resultMsg : 에러인 경우 에러 메세지
         **/
        // 2. 본인확인 토큰 요청
        String mokGetTokenResponse = this.sendRequestToken(tokenUrl, mokGetTokenResponseJsonString);


        assert mokGetTokenResponse != null;
        // JSONObject 객체로 변환
        com.dreamsecurity.json.JSONObject mokGetTokenResponseJson = new com.dreamsecurity.json.JSONObject(mokGetTokenResponse);

        String encodedMokGetTokenResponseJsonString;
        encodedMokGetTokenResponseJsonString = URLEncoder.encode(mokGetTokenResponseJson.toString(), StandardCharsets.UTF_8);

        JSONObject mokConfirmRequestData = new JSONObject(encodedMokGetTokenResponseJsonString);
        String mokConfirmRequest = "";
        String encMokToken = mokConfirmRequestData.optString("encryptMOKToken", null);

        if (encMokToken == null) {
            setErrorMsg("본인확인 요청 MOKToken 이 없습니다.");
        }
        
        // 3. 인증 검증 요청

    }
}
