package com.fgama.pillowtalk.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
public class ChattingMessage extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "chatting_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_room_id")
    private ChattingRoom chattingRoom;

    private Long number;

    private Boolean isRead;

    private String resourceUrl;

    @Column(name = "chatting_message_type")
    private String type;
    @Column(length = 5000)
    private String message;
    private Long questionIndex;
    private Long challengeIndex;
    private String emoji;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
}
