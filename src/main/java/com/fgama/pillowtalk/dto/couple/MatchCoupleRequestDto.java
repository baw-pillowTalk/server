package com.fgama.pillowtalk.dto.couple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchCoupleRequestDto {
    @NotBlank(message = "초대 코드는 필수 입력값 입니다.")
    private String inviteCode;
}
