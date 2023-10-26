package com.fgama.pillowtalk.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Slf4j
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MemberImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_image_id")
    private Long id;

    private String file_name;

    private String imagePath;

    private String url;

    @OneToOne(mappedBy = "memberImage", fetch = FetchType.LAZY)
    private Member member;
}