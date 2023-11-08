package com.fgama.pillowtalk.domain;

import com.fgama.pillowtalk.constant.MemberStatus;
import com.fgama.pillowtalk.constant.Role;
import com.fgama.pillowtalk.constant.SnsType;
import com.fgama.pillowtalk.dto.member.UpdateMySignalRequestDto;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String oauthId;
    private String nickname;

    @Column(unique = true)
    private String refreshToken;

    private Long coupleId;
    private String fcmToken;
    @Column(unique = true)
    private String inviteCode;
    private String emoTitle;

    private Integer signal;

    private Boolean chattingRoomStatus;
    private Integer nicknameChangeCount;
    private Boolean marketingConsent;
    //android apple sns 로그인시 사용
    private String state;

    @Enumerated(EnumType.STRING)
    private MemberStatus memberStatus;

    @Enumerated(EnumType.STRING)
    private SnsType snsType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_image_id")
    private MemberImage memberImage;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_config_id")
    private MemberConfig memberConfig;

    //기존에 쓰다가 안쓰는중 쓸 수도 있음
    private String os;

    //기존에 질문 기능을 위해 있던 코드
    @OneToMany(mappedBy = "member")
    private List<ChattingMessage> messageList = new ArrayList<>();

    public void deleteRefreshToken() {
        this.refreshToken = null;
    }

    public void updateMemberImage(MemberImage memberImage) {
        this.memberImage = memberImage;
    }

    public void updateMemberNickname(String nickName) {
        this.nickname = nickName;
    }

    public void logout() {
        this.fcmToken = null;
    }

    public Void updateSignal(UpdateMySignalRequestDto request) {
        this.signal = request.getMySignal();
        return null;
    }
}