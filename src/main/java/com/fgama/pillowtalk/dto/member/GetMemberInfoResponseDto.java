package com.fgama.pillowtalk.dto.member;

import com.fgama.pillowtalk.constant.SnsType;
import com.fgama.pillowtalk.domain.Member;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetMemberInfoResponseDto {
    private String nickName;
    private SnsType snsType;

    public static GetMemberInfoResponseDto from(Member member) {
        return GetMemberInfoResponseDto.builder()
                .nickName(member.getNickname())
                .snsType(member.getSnsType())
                .build();
    }
}
