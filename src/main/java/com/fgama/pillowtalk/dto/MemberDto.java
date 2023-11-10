package com.fgama.pillowtalk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MemberDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class CreateMemberRequestV1 {
        private String state; // android + apple
        private Boolean marketingConsent;
        private String nickname;
        private String fcmToken;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class CreateMemberGOOGLERequestV1 {
        private String idToken;
        private String authCode;
        private Boolean marketingConsent;
        private String nickName;
        private String fcmToken;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class CreateMemberKAKAORequestV1 {
        private String refreshToken;
        private Boolean marketingConsent;
        private String nickName;
        private String fcmToken;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class CreateMemberNAVERRequestV1 {
        private String refreshToken;
        private Boolean marketingConsent;
        private String nickName;
        private String fcmToken;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class CreateMemberAPPLEIOSRequestV1 {
        private String authorizationCode;
        private Boolean marketingConsent;
        private String nickName;
        private String fcmToken;
    }

    @Data
    @AllArgsConstructor
    static public class CreateMemberAPPLEANDROIDRequestV1 {
        private String state;
        private Boolean marketingConsent;
        private String nickName;
        private String fcmToken;
    }


    @Data
    @AllArgsConstructor
    static public class CreateMemberResponseV1 {
        private String status;
        private String message;
        private Long expiresIn;
        private String accessToken;
        private String refreshToken;
        private String annotation;
    }


    @Data
    public static class AppleTokenResponse {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private Long expiresIn;

        @JsonProperty("id_token")
        private String idToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("token_type")
        private String tokenType;

        // Getters and setters
    }

}