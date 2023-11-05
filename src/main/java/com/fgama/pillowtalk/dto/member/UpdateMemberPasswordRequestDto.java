package com.fgama.pillowtalk.dto.member;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberPasswordRequestDto {
    @NotBlank
    private String password;
}
