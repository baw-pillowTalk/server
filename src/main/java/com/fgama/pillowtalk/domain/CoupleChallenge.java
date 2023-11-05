package com.fgama.pillowtalk.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE COUPLE_CHALLENGE SET COUPLE_CHALLENGE.DELETED_AT = CURRENT_TIMESTAMP WHERE COUPLE_CHALLENGE.CHALLENGE_ID = ?")
@Where(clause = "DELETED_AT is null")
@Setter
@Entity
public class CoupleChallenge extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "challenge_id")
    private Long id;
    private Long number;
    private String title;
    private String body;
    private LocalDateTime targetDate;
    private Boolean done;
    private String creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @Builder
    public CoupleChallenge(Long id, String title, String body, LocalDateTime targetDate, Boolean done, Couple couple, String creator, Long number, LocalDateTime createAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.targetDate = targetDate;
        this.done = done;
        this.couple = couple;
        this.creator = creator;
        this.number = number;
    }
}
