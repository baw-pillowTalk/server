package com.fgama.pillowtalk.dto.adult;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class AdultAuthenticationVerificationRequestDto {
    @NotBlank
    private String authNumber;

    public AdultAuthenticationVerificationRequestDto(String authNumber) {
        this.authNumber = authNumber;
    }
}
