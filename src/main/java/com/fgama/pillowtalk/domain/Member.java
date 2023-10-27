package com.fgama.pillowtalk.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true)
    private String oauthId;
    private String nickname;
    //회원 정보
    private String accessToken;
    private String refreshToken;
    private Long coupleId;
    private String fcmToken;
    private String snsType;
    private String inviteCode;
    private Long expiresIn;
    private String loginState;
    private String emoTitle; //시그널과 연동
    //설정값
    private Boolean chattingRoomStatus;
    private Integer nicknameChangeCount;
    private Boolean marketingConsent;
    //android apple sns 로그인시 사용
    private String state;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_image_id")
    private MemberImage memberImage;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_config_id")
    private MemberConfig memberConfig;

    //기존에 쓰다가 안쓰는중 쓸 수도 있음
    private String os;

    //기존에 질문 기능을 위해 있던 코드
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<ChattingMessage> messageList = new ArrayList<>();
}