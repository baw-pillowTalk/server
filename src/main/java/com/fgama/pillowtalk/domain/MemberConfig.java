package com.fgama.pillowtalk.domain;

import com.fgama.pillowtalk.dto.member.SetMemberPasswordRequestDto;
import com.fgama.pillowtalk.dto.member.UpdateMemberPasswordRequestDto;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class MemberConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_config_id")
    private Long id;
    private Boolean locked;
    @Column(length = 2000)
    private String password;
    private Boolean push;
    private Integer questionType;
    private String answer;
    private String language;
    private Boolean advertisement;
    private String version;

    @OneToOne(mappedBy = "memberConfig", fetch = FetchType.LAZY)
    private Member member;

    public void setMemberLanguage(String language) {
        this.language = language;
    }

    public Void setMemberPassword(SetMemberPasswordRequestDto request) {
        this.password = request.getPassword();
        this.questionType = request.getQuestionType();
        this.answer = request.getAnswer();
        return null;
    }

    public Void updateMemberPassword(UpdateMemberPasswordRequestDto request) {
        this.password = request.getPassword();
        return null;
    }
}