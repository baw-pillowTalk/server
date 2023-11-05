package com.fgama.pillowtalk.dto.member;

import com.fgama.pillowtalk.constant.SnsType;
import com.fgama.pillowtalk.domain.Member;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPartnerInfoResponseDto {
    private String nickName;
    private SnsType snsType;

    public static GetPartnerInfoResponseDto from(Member partner) {
        return GetPartnerInfoResponseDto.builder()
                .nickName(partner.getNickname())
                .snsType(partner.getSnsType())
                .build();
    }
}
