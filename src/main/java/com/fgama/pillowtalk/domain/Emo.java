package com.fgama.pillowtalk.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Emo {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "emo_id")
    private Long id;

    private String url;
    private String title;
    private String body;



}
