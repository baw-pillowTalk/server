package com.fgama.pillowtalk.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Couple {

    @Id
    @GeneratedValue
    @Column(name = "couple_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "self_member_id")
    private Member self;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_member_id")
    private Member partner;

    private LocalDateTime createdAt;

    private String status; //available unavailable breakup

    @OneToMany(mappedBy = "couple" , fetch = FetchType.LAZY)
    private List<ChattingRoom> chattingRooms = new ArrayList<>();

    @OneToMany(mappedBy = "couple" , fetch = FetchType.LAZY)
    private List<CoupleQuestion> coupleQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "couple" , fetch = FetchType.LAZY)
    private List<Challenge> challenges = new ArrayList<>();

    private String coupleCode;

}
