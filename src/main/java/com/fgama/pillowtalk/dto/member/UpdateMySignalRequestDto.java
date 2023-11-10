package com.fgama.pillowtalk.dto.member;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMySignalRequestDto {
    @NotNull
    @Min(0)
    @Max(100)
    private Integer mySignal;
}
