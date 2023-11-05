package com.fgama.pillowtalk.dto.member;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeChattingRoomStateRequestDto {
    private boolean isInChat;
}
