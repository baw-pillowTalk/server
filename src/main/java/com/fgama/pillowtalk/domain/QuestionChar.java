package com.fgama.pillowtalk.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class QuestionChar {
    @Id
    @GeneratedValue
    @Column(name = "question_char_id")
    private Long id;
    private Long number;
    private Character ch;
    private String type;
    private String color;
    private Boolean bold;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "question_id")
    private Question question;

    @Builder
    public QuestionChar(Long id, Long number, Character ch, String type, String color, boolean bold, Question question) {
        this.id = id;
        this.number = number;
        this.ch = ch;
        this.type = type;
        this.color = color;
        this.bold = bold;
        this.question = question;
    }
}
