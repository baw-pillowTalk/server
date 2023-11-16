package com.fgama.pillowtalk.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckMemberAnswerValidationRequestDto {
    @NotBlank
    private String answer;
}
