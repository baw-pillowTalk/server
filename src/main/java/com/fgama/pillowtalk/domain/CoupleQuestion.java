package com.fgama.pillowtalk.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class CoupleQuestion {
    @Id
    @GeneratedValue
    @Column(name = "couple_question_id")
    private Long id;

    private int number;// index

    @Column(length=5000)
    private String selfAnswer;
    @Column(length=5000)
    private String partnerAnswer;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
}
