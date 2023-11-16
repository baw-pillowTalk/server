package com.fgama.pillowtalk.domain;

import com.fgama.pillowtalk.dto.member.SetMemberPasswordRequestDto;
import com.fgama.pillowtalk.dto.member.UpdateMemberPasswordRequestDto;
import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class MemberConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_config_id")
    private Long id;
    private boolean isLocked;
    @Column(length = 2000)
    private String password;
    private Boolean push;
    private Integer questionType;
    private String answer;
    private String language;
    private Boolean advertisement;
    private String version;


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