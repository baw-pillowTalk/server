package com.fgama.pillowtalk.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue
    @Column(name = "question_id")
    private Long id;
    private int number;
    private String title;

    private String header;
    private String body;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<CoupleQuestion> coupleQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<QuestionChar> questionChars = new ArrayList<>();

    @Builder
    public Question(Long id, int number, String title, String header, String body, List<QuestionChar> questionChars) {
        this.id = id;
        this.number = number;
        this.title = header + body;
        this.header = header;
        this.body = body;
        this.questionChars = questionChars;
    }

    public List<Long> getHighLight() {
        List<Long> highlight = new ArrayList<>();
        for (QuestionChar questionChar : this.questionChars) {
            if (questionChar.getColor() == "purple") {
                highlight.add(questionChar.getNumber());
            }
        }
        return highlight;

    }
}
