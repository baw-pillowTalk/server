package com.fgama.pillowtalk.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {
    @Id
    @GeneratedValue
    @Column(name = "challenge_id")
    private Long id;
    private Long number;
    private String category;
    private String title;
    private String body;
    private LocalDateTime createAt;
    private LocalDateTime targetDate;
    private Boolean done;
    private String creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @Builder
    public Challenge(Long id, String title,String category, String body, LocalDateTime targetDate, Boolean done, Couple couple, String creator,Long number,LocalDateTime createAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.body = body;
        this.targetDate = targetDate;
        this.done = done;
        this.couple = couple;
        this.creator = creator;
        this.number = number;
        this.createAt = createAt;
    }
}
