package com.fgama.pillowtalk.domain;

import lombok.*;

import javax.persistence.*;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class ChattingRoom {
    @Id
    @GeneratedValue
    @Column(name = "chatting_room_id")
    private Long id;

    private String title;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @OneToMany(mappedBy = "chattingRoom")
    private List<ChattingMessage> messageList;
}