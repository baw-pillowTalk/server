package com.fgama.pillowtalk.dto.member;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetProfileImageResponseDto {
    private String fileName;
    private String imagePath;
    private String url;
}
