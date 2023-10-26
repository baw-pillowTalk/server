package com.fgama.pillowtalk.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class PillowTalkBoard {
    @Id
    @GeneratedValue
    @Column(name = "pillow_talk_board_id")
    private Long id;

    private String title;
    private String body;
    private String email;
    private LocalDateTime createAt;
    private String creator;
}
