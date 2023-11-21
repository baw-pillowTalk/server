package com.fgama.pillowtalk.domain;

import com.fgama.pillowtalk.constant.MemberStatus;
import com.fgama.pillowtalk.constant.Role;
import com.fgama.pillowtalk.constant.SnsType;
import com.fgama.pillowtalk.domain.chattingMessage.ChattingMessage;
import com.fgama.pillowtalk.dto.member.ChangeChattingRoomStateRequestDto;
import com.fgama.pillowtalk.dto.member.UpdateMySignalRequestDto;
import com.fgama.pillowtalk.dto.signup.CompleteMemberSignupRequestDto;
import lombok.*;

import javax.persistence.*;
import java.util.List;

import static com.fgama.pillowtalk.constant.MemberStatus.SOLO;

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
    @Column(name = "member_signal")
    private Integer signal;
    private Boolean chattingRoomStatus;
    private Integer nicknameChangeCount;
    private Boolean marketingConsent;

    //android apple sns 로그인시 사용
    @Column(name = "member_state")
    private String state;

    @Enumerated(EnumType.STRING)
    private MemberStatus memberStatus;

    @Enumerated(EnumType.STRING)
    private SnsType snsType;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MemberImage memberImage;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MemberConfig memberConfig;

    //기존에 쓰다가 안쓰는중 쓸 수도 있음
    private String os;

    //기존에 질문 기능을 위해 있던 코드
    @OneToMany(mappedBy = "member")
    private List<ChattingMessage> messageList;


    public void updateMemberImage(MemberImage memberImage) {
        this.memberImage = memberImage;
    }

    public void updateMemberNickname(String nickName) {
        this.nickname = nickName;
    }

    public void logout() {
        this.fcmToken = null;
        this.refreshToken = null;
    }

    public void updateSignal(UpdateMySignalRequestDto request) {
        this.signal = request.getMySignal();
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long completeMemberSignup(String inviteCode,
                                     CompleteMemberSignupRequestDto request,
                                     MemberImage memberImage,
                                     MemberConfig memberConfig) {
        this.nickname = request.getNickname();
        this.marketingConsent = request.getMarketingConsent();
        this.fcmToken = request.getFcmToken();
        this.memberStatus = SOLO;
        this.inviteCode = inviteCode;
        this.chattingRoomStatus = false;
        this.signal = 50;
        this.memberImage = memberImage;
        this.memberConfig = memberConfig;
        return this.id;
    }

    public void setChattingRoomStatus(ChangeChattingRoomStateRequestDto request) {
        this.chattingRoomStatus = request.isInChat();
    }
}