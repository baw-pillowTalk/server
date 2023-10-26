package com.fgama.pillowtalk.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ChattingRoom {
    @Id
    @GeneratedValue
    @Column(name = "chatting_room_id")
    private Long id;

    private String title;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @OneToMany(mappedBy = "chattingRoom", fetch = FetchType.LAZY)
    private List<ChattingMessage> messageList = new ArrayList<>();

}