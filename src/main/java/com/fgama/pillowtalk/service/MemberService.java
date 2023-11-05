package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.components.FileDetail;
import com.fgama.pillowtalk.constant.MemberStatus;
import com.fgama.pillowtalk.constant.SnsType;
import com.fgama.pillowtalk.domain.*;
import com.fgama.pillowtalk.dto.member.*;
import com.fgama.pillowtalk.exception.couple.CoupleNotFoundException;
import com.fgama.pillowtalk.exception.member.MemberNotFoundException;
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

import static com.fgama.pillowtalk.constant.MemberStatus.SOLO;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final ChattingMessageRepository chattingMessageRepository;
    private final CoupleRepository coupleRepository;
    private final FileUploadService fileUploadService;

    @Value("${service.image-url}")
    private String serviceImageUrl;
    @Value("${service.default-image-url}")
    private String serviceDefaultImageUrl;

    public Long join(Member member) {
//        validateDuplicateEamilAndSnsType(member);
//        Member member1 = memberCustomRepository.save(member);
        Member member1 = memberRepository.save(member);
        return member1.getId();
    }

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
    @Transactional(readOnly = true)
    public Member findMemberByOauthIdAndSnsType(String oauthId, SnsType snsType) {
        return memberRepository.findMemberByOauthIdAndSnsType(oauthId, snsType)
                .orElseThrow(() -> new MemberNotFoundException("일치하는 회원이 존재하지 않습니다."));

    }

    @Transactional
    public void changeChatRoomStatus(Boolean isInChat) throws NullPointerException {
        Member member = this.getCurrentMember();
        member.setChattingRoomStatus(isInChat);
        if (isInChat) {
            Couple couple = this.coupleRepository.findById(member.getCoupleId()).orElseThrow(NullPointerException::new);
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
            List<ChattingMessage> chattingMessages = couple.getChattingRoom().getMessageList();

            for (ChattingMessage message : chattingMessages) {
                if (message.getMember() == partner) {
                    message.setIsRead(true);
                }
            }
        }
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
        switch (message.getType()) {
            case "text":
                result = message.getMessage();
                break;
            case "image":
                result = "이미지 채팅입니다.";
                break;
            case "video":
                result = "동영상 채팅입니다.";
                break;
            case "voice":
                result = "보이스 채팅입니다.";
                break;
            case "question":
                result = "질문 채팅입니다.";
                break;
            case "challenge":
                result = "챌린지 채팅입니다.";
                break;
            case "emoji":
                result = "이모티콘 채팅입니다.";
                break;
            default:
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

    public void updatePassword(String accessToken, String password) throws RuntimeException {
        Member member = this.getCurrentMember();
        if (member.getMemberConfig().getLock()) {
            member.getMemberConfig().setPassword(password);
        } else {
            throw new RuntimeException("비번 초기 값이 없습니다.");

        }
        memberRepository.save(member);
    }

    public void unlockPassword(String accessToken) throws RuntimeException {
        Member member = this.getCurrentMember();
        if (member.getMemberConfig().getLock()) {
            member.getMemberConfig().setLock(false);
            member.getMemberConfig().setPassword(null);
            member.getMemberConfig().setAnswer(null);
            member.getMemberConfig().setQuestionType(null);
        } else {
            throw new RuntimeException("잠금이 아닙니다.");
        }
        memberRepository.save(member);
    }

    public MemberConfig getPasswordData(String accessToken) throws RuntimeException {

        Member member = this.getCurrentMember();
        if (member.getMemberConfig().getLock()) {
            return member.getMemberConfig();
        } else {
            throw new RuntimeException("잠금상태가 아닙니다.");
        }
    }


    public void checkPassword(String accessToken, String password) throws RuntimeException {
        Member member = this.getCurrentMember();
        if (!password.equals(member.getMemberConfig().getPassword())) {
            throw new RuntimeException("비밀번호 틀림");
        }

    }

    public boolean validAnswer(String accessToken, String answer) throws RuntimeException {
        Member member = this.getCurrentMember();
        return answer.equals(member.getMemberConfig().getAnswer());

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
        return member.getMemberImage().toGetPartnerImageResponseDto();
    }

    @Transactional
    public void updateNickname(UpdateNicknameRequestDto request) throws RuntimeException {
        this.getCurrentMember().setNickname(request.getNickname());
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
     * = 회원 언어 설정하기
     **/
    @Transactional
    public Void setMemberLanguage(SetMemberLanguageRequestDto request) {
        this.getCurrentMemberConfig().setMemberLanguage(request.getLanguage());
        return null;
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
    public Void setMemberPassword(SetMemberPasswordRequestDto request) {
        return this.getCurrentMemberConfig().setMemberPassword(request);
    }

    /**
     * - 회원 비밀번호 업데이트
     **/
    @Transactional
    public Void updateMemberPassword(UpdateMemberPasswordRequestDto request) {
        return this.getCurrentMemberConfig().updateMemberPassword(request);
    }


    private Couple getCouple(Member member) {
        return coupleRepository.findCoupleById(member.getCoupleId())
                .orElseThrow(() -> new CoupleNotFoundException("일치하는 커플 데이터가 존재하지 않습니다."));
    }
}