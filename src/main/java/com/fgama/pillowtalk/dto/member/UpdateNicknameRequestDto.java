package com.fgama.pillowtalk.dto.member;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNicknameRequestDto {
    @NotBlank
    private String nickname;
}
