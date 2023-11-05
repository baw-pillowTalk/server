package com.fgama.pillowtalk.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE COUPLE_QUESTION SET COUPLE_QUESTION.DELETED_AT = CURRENT_TIMESTAMP WHERE COUPLE_QUESTION.COUPLE_QUESTION_ID = ?")
@Where(clause = "DELETED_AT is null")
@Entity
public class CoupleQuestion extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "couple_question_id")
    private Long id;

    private int number;// index

    @Column(length = 5000)
    private String selfAnswer;
    @Column(length = 5000)
    private String partnerAnswer;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
}
