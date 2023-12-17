package com.fgama.pillowtalk.dto.adult;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * - 본인 확인 API 거래 토큰 요청
 **/
@Getter
@NoArgsConstructor
public class AdultAuthenticationTokenDto {


    @Getter
    @NoArgsConstructor
    public static class AdultAuthenticationRequestDto {
        @NotBlank
        private String providerId; // SKT
        @NotBlank
        private String reqAuthType; // SMS
        @NotBlank
        private String userName; // 사용자 이름
        @NotBlank
        private String userPhone; // 사용자 번호
        @NotBlank
        private String userBirthday; // 사용자 생년월일
        @NotBlank
        private String userGender; // 사용자 성별
        @NotBlank
        private String userNation; // 내국인 : 0 & 외국인 : 1

        @Builder
        public AdultAuthenticationRequestDto(String providerId, String reqAuthType,
                                             String userName, String userPhone,
                                             String userBirthday, String userGender, String userNation
        ) {
            this.providerId = providerId;
            this.reqAuthType = reqAuthType;
            this.userName = userName;
            this.userPhone = userPhone;
            this.userBirthday = userBirthday;
            this.userGender = userGender;
            this.userNation = userNation;
        }
    }
}
