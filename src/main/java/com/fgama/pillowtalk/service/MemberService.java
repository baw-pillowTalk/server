package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.components.FileDetail;
import com.fgama.pillowtalk.constant.MemberStatus;
import com.fgama.pillowtalk.constant.SnsType;
import com.fgama.pillowtalk.domain.*;
import com.fgama.pillowtalk.domain.chattingMessage.*;
import com.fgama.pillowtalk.dto.member.*;
import com.fgama.pillowtalk.exception.couple.CoupleNotFoundException;
import com.fgama.pillowtalk.exception.member.*;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.repository.ChattingMessageRepository;
import com.fgama.pillowtalk.repository.CoupleRepository;
import com.fgama.pillowtalk.repository.MemberRepository;
import com.fgama.pillowtalk.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.fgama.pillowtalk.constant.MemberStatus.COUPLE;
import static com.fgama.pillowtalk.constant.MemberStatus.SOLO;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final ChattingMessageRepository chattingMessageRepository;
    private final CoupleRepository coupleRepository;

    private final FileUploadService fileUploadService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @Value("${service.image-url}")
    private String serviceImageUrl;
    @Value("${service.default-image-url}")
    private String serviceDefaultImageUrl;

    /**
     * - 해당 code 을 가지는 Member 정보 가져오기
     **/
    @Transactional(readOnly = true)
    public Member findMemberByInviteCode(String code) throws RuntimeException {
        return this.memberRepository.findMemberByInviteCode(code)
                .orElseThrow(() -> new MemberNotFoundException("일치하는 회원이 존재하지 않습니다."));
    }

    /**
     * - oauthId, snsType 에 해당하는 사용자 정보 가져오기
     **/
    public Member findMemberByOauthIdAndSnsType(String oauthId, SnsType snsType) {
        return memberRepository.findMemberByOauthIdAndSnsType(oauthId, snsType)
                .orElseThrow(() -> new MemberNotFoundException("일치하는 회원이 존재하지 않습니다."));

    }

    /**
     * - 채팅 방 상태 변경
     **/
    @Transactional
    public void changeChatRoomStatus(ChangeChattingRoomStateRequestDto request) throws NullPointerException {
        Member member = this.getCurrentMember();
        Couple couple = this.getCouple(member);
        member.setChattingRoomStatus(request);

        Member memberPartner = null;

        if (request.isInChat()) {
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
            List<ChattingMessage> chattingMessages = couple.getChattingRoom().getMessageList();

            for (ChattingMessage message : chattingMessages) {
                if (message.getMember() == partner) {
                    message.setIsRead(true);
                }
            }
            memberPartner = partner;
        }
        String fcmDetail = this.firebaseCloudMessageService.getFcmChattingStatus(
                "chatRoomStatusChange",
                request.isInChat()
        );
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, Objects.requireNonNull(memberPartner).getFcmToken());
    }

    public Member findMemberByRefreshToken(String refreshToken) throws NullPointerException {
        return memberRepository.findMemberByRefreshToken(refreshToken).orElseThrow(NullPointerException::new);
    }

    public String findPartnerLastMessage(String accessToken) throws NullPointerException {
        Member member = this.getCurrentMember();
        Couple couple = this.getCouple(member);
        Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
        ChattingMessage message = chattingMessageRepository.findFirstByMemberIdOrderByCreatedAtDesc(partner.getId());
        String result;


        if (message instanceof TextChattingMessage) {
            result = "텍스트 채팅입니다.";
        } else if (message instanceof ImageChattingMessage) {
            result = "이미지 채팅입니다.";
        } else if (message instanceof VoiceChattingMessage) {
            result = "보이스 채팅입니다.";
        } else if (message instanceof QuestionChattingMessage) {
            result = "질문 채팅입니다.";
        } else if (message instanceof ChallengeChattingMessage) {
            result = "챌린지 채팅입니다.";
        } else if (message instanceof CompleteChallengeChattingMessage) {
            result = "챌린지 채팅입니다.";
        } else if (message instanceof ResetPasswordChattingMessage) {
            result = "리셋요청 채팅입니다.";
        } else if (message instanceof SignalChattingMessage) {
            result = "시그널 채팅입니다.";
        }else if (message instanceof CompleteChallengeChattingMessage) {
            result = "챌린지 채팅입니다.";
        }else if (message instanceof PressForAnswerChattingMessage) {
            result = "질문 채팅입니다.";
        } else {
            throw new NullPointerException("연인 마지막 메시지가 없습니다.");
        }

        return result;
    }

    public Boolean IsPartnerLastMessageRead(String accessToken) throws NullPointerException {
        Member member = this.getCurrentMember();
        Couple couple = this.getCouple(member);
        Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
        ChattingMessage message = chattingMessageRepository.findFirstByMemberIdOrderByCreatedAtDesc(partner.getId());
        if (message != null) {
            return message.getIsRead();
        } else {
            throw new NullPointerException("연인 마지막 메시지가 없습니다.");
        }


    }

    @Transactional
    public void updateFcmToken(UpdateFcmTokenRequestDto request) throws NullPointerException {
        this.getCurrentMember().setFcmToken(request.getFcmToken());
    }


    public String getMyPosition(String accessToken) {
        Member member = this.getCurrentMember();
        Couple couple = getCouple(member);

        return (couple.getSelf() == member) ? "self" : "partner";
    }


    public Member SignUpAppleAndroid(Member member, Boolean marketingConsent, String nickName, String fcmToken) {
        member.setNickname(nickName);
        member.setMarketingConsent(marketingConsent);
        member.setFcmToken(fcmToken);
        member.setNickname(nickName);
        member.setMemberStatus(SOLO);

        return memberRepository.save(member);

    }

    @Transactional
    public void unlockPassword() throws RuntimeException {
        Member member = this.getCurrentMember();
        if (member.getMemberConfig().isLocked()) { // 이미 걸려 있는 경우
            member.getMemberConfig().setLocked(false);
            member.getMemberConfig().setPassword(null);
            member.getMemberConfig().setAnswer(null);
            member.getMemberConfig().setQuestionType(null);
        } else {
            throw new RuntimeException("잠금이 아닙니다.");
        }
    }

    public MemberConfig getPasswordData(String accessToken) throws RuntimeException {

        Member member = this.getCurrentMember();
        if (member.getMemberConfig().isLocked()) {
            return member.getMemberConfig();
        } else {
            throw new RuntimeException("잠금 상태가 아닙니다.");
        }
    }


    @Transactional
    public void checkPassword(UpdatePasswordRequestDto request) throws RuntimeException {
        Member member = this.getCurrentMember();
        if (!request.getPassword().equals(member.getMemberConfig().getPassword())) {
            throw new PasswordNotMatchException("비밀번호가 잁치하지 않습니다.");
        }
    }

    @Transactional
    public boolean validAnswer(CheckMemberAnswerValidationRequestDto request) throws RuntimeException {
        MemberConfig currentMemberConfig = this.getCurrentMemberConfig();
        if (currentMemberConfig.getAnswer() == null) {
            throw new AnswerNotFoundException("질문 답변이 존재하지 않습니다.");
        }
        return Objects.equals(currentMemberConfig.getAnswer(), request.getAnswer());

    }

    @Transactional
    public void updateToDefaultImage(Long defaultImage) {
        Member member = getCurrentMember();
        member.updateMemberImage(MemberImage.builder()
                .imagePath(serviceDefaultImageUrl + defaultImage + ".png")
                .fileName("default")
                .url(serviceDefaultImageUrl + defaultImage + ".png")
                .build());
    }

    @Transactional
    public void updateMyProfileImage(UpdateProfileImageRequestDto request) throws RuntimeException {
        FileDetail fileDetail = this.fileUploadService.save(request.getMemberProfileImage());
        MemberImage memberImage = MemberImage.builder()
                .fileName(fileDetail.getName())
                .imagePath(fileDetail.getPath())
                .url(serviceDefaultImageUrl + fileDetail.getPath())
                .build();
        Member member = this.getCurrentMember();
        member.updateMemberImage(memberImage);
    }

    public Boolean checkNicknameChangeExceed(String accessToken) throws RuntimeException {
        Member member = this.getCurrentMember();
        log.info(member.getNicknameChangeCount().toString());
        return member.getNicknameChangeCount() > 0;
    }

    public Member getPartnerByAccessToken(String accessToken) throws NullPointerException {
        Member findMember = this.getCurrentMember();
        Couple couple = getCouple(findMember);
        return (couple.getSelf() == findMember) ? couple.getPartner() : couple.getSelf();
    }


    @Transactional(readOnly = true)
    public GetProfileImageResponseDto getPartnerProfileImage() {
        Member member = this.getCurrentMember();
        Couple couple = this.getCouple(member);
        Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
        return partner.getMemberImage().toGetPartnerImageResponseDto();
    }

    @Transactional(readOnly = true)
    public GetProfileImageResponseDto getMyProfileImage() {
        Member member = this.getCurrentMember();
        MemberImage memberImage = member.getMemberImage();
        if (memberImage == null) {
            return null;
        }
        return memberImage.toGetPartnerImageResponseDto();
    }

    @Transactional
    public void updateNickname(UpdateNicknameRequestDto request) throws RuntimeException {
        Member currentMember = this.getCurrentMember();
        if (currentMember.getNickname().equals(request.getNickname())) {
            throw new NickNameEqualException("기존 닉네임과 동일합니다.");
        }
        currentMember.setNickname(request.getNickname());
    }

    /**
     * - 처음 앱 구동시 호출 되는 회원 상태 호출 API
     **/
    @Transactional(readOnly = true)
    public MemberStatus getMemberStatus() {
        return this.getCurrentMember().getMemberStatus();
    }

    /**
     * - 회원 초대 코드 가져오기 API
     **/
    @Transactional(readOnly = true)
    public String getInviteCode() {
        return this.getCurrentMember().getInviteCode();
    }

    public Member getMemberById(Long id) {
        return this.memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("일치하는 회원이 존재하지 않습니다."));
    }

    public Member getCurrentMember() {
        return this.getMemberById(SecurityUtil.getMemberId());
    }



    public MemberConfig getCurrentMemberConfig() {
        return this.getCurrentMember().getMemberConfig();
    }


    /**
     * - 회원 정보 가져오기
     **/
    @Transactional(readOnly = true)
    public GetMemberInfoResponseDto getMemberInfo() {
        return GetMemberInfoResponseDto.from(this.getCurrentMember());
    }

    /**
     * - 파트너 정보 가져오기
     **/
    @Transactional(readOnly = true)
    public GetPartnerInfoResponseDto getPartnerInfo() {
        Member self = this.getCurrentMember();
        Couple couple = this.getCouple(self);
        return GetPartnerInfoResponseDto.from(
                (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf()
        );
    }

    /**
     * - 회원 설정 정보 가져오기
     **/
    @Transactional(readOnly = true)
    public GetMemberConfigInfoResponseDto getMemberConfig() {
        return GetMemberConfigInfoResponseDto.from(this.getCurrentMemberConfig());
    }

    /**
     * - 회원 언어 설정하기
     **/
    @Transactional
    public void setMemberLanguage(SetMemberLanguageRequestDto request) {
        this.getCurrentMemberConfig().setMemberLanguage(request.getLanguage());
    }

    /**
     * - 회원 비밀번호 가져오기
     **/
    @Transactional(readOnly = true)
    public String getMemberPassword() {
        return this.getCurrentMemberConfig().getPassword();
    }

    /**
     * - 회원 비밀번호 설정하기
     **/
    @Transactional
    public void setMemberPassword(SetMemberPasswordRequestDto request) {
        MemberConfig memberConfig = this.getCurrentMemberConfig();
        if (memberConfig.getPassword() != null) {
            throw new PasswordAlreadyExistException("기존 비밀번호가 존재합니다.");
        }
        memberConfig.setMemberPassword(request);
    }

    /**
     * - 회원 비밀번호 업데이트
     **/
    @Transactional
    public void updateMemberPassword(UpdateMemberPasswordRequestDto request) {
        MemberConfig config = this.getCurrentMemberConfig();
        if (config.getPassword() == null) {
            throw new PasswordNotFoundException("기존 비밀번호가 존재하지 않습니다.");
        }
        config.updateMemberPassword(request);
    }

    /**
     * - 회원 답변 질문 가져오기
     **/
    @Transactional(readOnly = true)
    public int getMemberQuestionType() {
        return this.getCurrentMemberConfig().getQuestionType();
    }


    /**
     * - 회원 시그널 가져오기
     **/
    @Transactional(readOnly = true)
    public GetSignalInfoResponseDto getMySignal() {
        Member member = this.getCurrentMember();
        return new GetSignalInfoResponseDto(member.getSignal());
    }

    /**
     * - 회원 파트너 시그널 가져오기
     **/
    @Transactional(readOnly = true)
    public GetSignalInfoResponseDto getPartnerSignal() {
        Member member = this.getCurrentMember();
        Couple couple = this.getCouple(member);
        return (couple.getSelf() == member) ?
                new GetSignalInfoResponseDto(couple.getPartner().getSignal())
                : new GetSignalInfoResponseDto(couple.getSelf().getSignal());
    }

    /**
     * - 회원 시그널 수정하기
     **/
    @Transactional
    public void updateMemberSignal(UpdateMySignalRequestDto request) {
        Member member = this.getCurrentMember();
        member.updateSignal(request);
    }


    public Couple getCouple(Member member) {
        return coupleRepository.findCoupleById(member.getCoupleId())
                .orElseThrow(() -> new CoupleNotFoundException("일치하는 커플 데이터가 존재하지 않습니다."));
    }

}