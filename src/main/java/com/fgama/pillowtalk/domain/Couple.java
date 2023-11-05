package com.fgama.pillowtalk.domain;

import com.fgama.pillowtalk.constant.CoupleStatus;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE COUPLE SET COUPLE.DELETED_AT = CURRENT_TIMESTAMP WHERE COUPLE.COUPLE_ID = ?")
@Where(clause = "DELETED_AT is null")
@Entity
public class Couple extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "couple_id")
    private Long id;

    private LocalDateTime deletedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "self_member_id")
    private Member self;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_member_id")
    private Member partner;

    @Enumerated(EnumType.STRING)
    private CoupleStatus coupleStatus; //available unavailable breakup


    /* 채팅 룸 */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "chatting_room_id")
    private ChattingRoom chattingRoom;

    /* 커플 질문 */
    @OneToMany(mappedBy = "couple", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CoupleQuestion> coupleQuestions = new ArrayList<>();

    /* 커플 챌린지 */
    @OneToMany(mappedBy = "couple", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CoupleChallenge> coupleChallenges = new ArrayList<>();

    private String coupleCode;
}