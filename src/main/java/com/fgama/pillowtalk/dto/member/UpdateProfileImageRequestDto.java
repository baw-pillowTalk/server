package com.fgama.pillowtalk.dto.member;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileImageRequestDto {
    private MultipartFile memberProfileImage;
}
