package com.fgama.pillowtalk.dto.member;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordRequestDto {
    @Pattern(regexp = "^\\d{4}$", message = "비밀번호는 4자리 숫자 입니다.")
    private String password;
    private int questionType;
    @NotBlank
    private String answer;
}
