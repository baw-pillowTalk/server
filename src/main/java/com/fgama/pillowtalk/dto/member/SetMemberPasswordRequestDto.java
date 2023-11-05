package com.fgama.pillowtalk.dto.member;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetMemberPasswordRequestDto {
    @NotBlank
    private String password;
    @NotNull
    private Integer questionType;
    @NotBlank
    private String answer;
}
