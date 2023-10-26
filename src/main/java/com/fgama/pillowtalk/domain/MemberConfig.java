package com.fgama.pillowtalk.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class MemberConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_config_id")
    private Long id;
    @ColumnDefault("false")
    private Boolean lock;
    @Column(length = 2000)
    private String password;
    private Boolean push;
    private String questionType;
    private String answer;
    private String language;
    private Boolean advertisement;
    private String version;

    @OneToOne(mappedBy = "memberConfig", fetch = FetchType.LAZY)
    private Member member;
}