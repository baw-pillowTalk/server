package com.fgama.pillowtalk.dto.member;


import com.fgama.pillowtalk.domain.MemberConfig;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetMemberConfigInfoResponseDto {
    private String language;
    private Boolean lock;

    public static GetMemberConfigInfoResponseDto from(MemberConfig memberConfig) {
        return GetMemberConfigInfoResponseDto.builder()
                .language(memberConfig.getLanguage())
                .lock(memberConfig.getLocked())
                .build();
    }
}
