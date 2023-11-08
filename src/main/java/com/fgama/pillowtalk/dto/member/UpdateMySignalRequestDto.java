package com.fgama.pillowtalk.dto.member;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMySignalRequestDto {
    @NotNull
    private Integer mySignal;
}
