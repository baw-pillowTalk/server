package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.components.FileDetail;
import com.fgama.pillowtalk.domain.*;
import com.fgama.pillowtalk.exceptions.MemberNotFoundException;
import com.fgama.pillowtalk.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    public static final String REGEXP = "^[a-z|A-Z|ㄱ-ㅎ|가-힣]{1,10}+$";
    private final MemberRepository memberRepository;
    private final ChattingRoomRepository chattingRoomRepository;
    private final MemberConfigRepository memberConfigRepository;
    private final ChattingMessageRepository chattingMessageRepository;
    private final ChallengeRepository challengeRepository;
    private final CoupleRepository coupleRepository;
    private final CoupleQuestionRepository coupleQuestionRepository;
    private final FileUploadService fileUploadService;

    public Long join(Member member) {
//        validateDuplicateEamilAndSnsType(member);
//        Member member1 = memberCustomRepository.save(member);
        Member member1 = memberRepository.save(member);
        return member1.getId();
    }

    public Optional<Member> findOptionalMemberByState(String state) {
        Optional<Member> findMember = memberRepository.findOptionalMemberByState(state);
        return findMember;
    }

    public Member findOptionalMemberByInviteCode(String code) throws RuntimeException {
        Optional<Member> findMember = memberRepository.findOptionalMemberByInviteCode(code);
        return findMember.orElseThrow(() -> new RuntimeException("not found member by generateCode"));
    }

    public Optional<Member> findOptionalMemberByUniqueId(String uniqueId) {
        Optional<Member> optionalMemberByName = memberRepository.findOptionalMemberByUniqueId(uniqueId);
        return optionalMemberByName;
    }

    public Member findOptionalMemberByUniqueIdV1(String uniqueId) throws NullPointerException {
        Member optionalMemberByName = memberRepository.findOptionalMemberByUniqueId(uniqueId).orElseThrow(NullPointerException::new);
        return optionalMemberByName;
    }

    public Member findMemberByUniqueId(String uniqueId) {
        return memberRepository.findMemberByUniqueId(uniqueId);

    }

    public Optional<Member> findOptionalMemberByAccessToken(String accessToken) {
        Optional<Member> optionalMemberByAccessToken = memberRepository.findOptionalMemberByAccessToken(accessToken);
        return optionalMemberByAccessToken;
    }

    public Member findMemberByAccessTokenThrow(String accessToken) {
        return memberRepository.findOptionalMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
    }

    public void changeChatRoomStatus(String accessToken, Boolean isInChat) throws NullPointerException {
        Member member = memberRepository.findOptionalMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        member.setChattingRoomStatus(isInChat);
        if (isInChat) {
            Couple couple = coupleRepository.findById(member.getCoupleId()).orElseThrow(NullPointerException::new);
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
            List<ChattingMessage> chattingMessages = couple.getChattingRooms().get(0).getMessageList();

            for (ChattingMessage message : chattingMessages) {
                if (message.getMember() == partner) {
                    message.setIsRead(true);
                }
            }

            coupleRepository.save(couple);
        }
        log.info("chatting room status 변경 :" + member.getUniqueId() + "현재상태 :" + isInChat);
        memberRepository.save(member);
    }

    public Member findMemberByRefreshToken(String refreshToken) throws NullPointerException {
        return memberRepository.findMemberByRefreshToken(refreshToken).orElseThrow(NullPointerException::new);
    }

    public Member findMemberByAccessToken(String accessToken) throws NullPointerException {
        Optional<Member> findMember = memberRepository.findMemberByAccessToken(accessToken);
        return findMember.orElseThrow(() -> new MemberNotFoundException("not found member by accessToken"));
    }

    public String findPartnerLastMessage(String accessToken) throws NullPointerException {
        Optional<Member> findMember = memberRepository.findMemberByAccessToken(accessToken);
        Member member = findMember.orElseThrow(() -> new MemberNotFoundException("not found member by accessToken"));
        Couple couple = coupleRepository.findCoupleById(member.getCoupleId());
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
        Optional<Member> findMember = memberRepository.findMemberByAccessToken(accessToken);
        Member member = findMember.orElseThrow(() -> new MemberNotFoundException("not found member by accessToken"));
        Couple couple = coupleRepository.findCoupleById(member.getCoupleId());
        Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
        ChattingMessage message = chattingMessageRepository.findFirstByMemberIdOrderByCreatedAtDesc(partner.getId());
        if (message != null) {
            return message.getIsRead();
        } else {
            throw new NullPointerException("연인 마지막 메시지가 없습니다.");
        }


    }

    public void setFcmToken(String accessToken, String fcmToken) throws NullPointerException {
        Optional<Member> findMember = memberRepository.findMemberByAccessToken(accessToken);
        Member member = findMember.orElseThrow(() -> new MemberNotFoundException("not found member by accessToken"));
        member.setFcmToken(fcmToken);
        memberRepository.save(member);
    }

    public Member getPartnerByAccessToken(String accessToken) throws NullPointerException {
        Member findMember = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        Couple couple = coupleRepository.findCoupleById(findMember.getCoupleId());

        return (couple.getSelf() == findMember) ? couple.getPartner() : couple.getSelf();
    }

    public String getMyPosition(String accessToken) {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        Couple couple = coupleRepository.findCoupleById(member.getCoupleId());

        return (couple.getSelf() == member) ? "self" : "partner";
    }

    public Member updateNickNameByAccessToken(String accessToken, String nickName) throws IllegalArgumentException {
        Optional<Member> findMember = memberRepository.findMemberByAccessToken(accessToken);
        if (findMember.isPresent()) {
            Member member = findMember.get();
            member.setNickname(nickName);
            member = memberRepository.save(member);
            return member;
        } else {
            throw new IllegalArgumentException("Invalid access token");
        }
    }

    @Transactional
    public void delelteMember(Member member) {
        coupleRepository.deleteById(member.getCoupleId());
        memberRepository.delete(member);
    }

    @Transactional
    public void deleteMember(Member member) {
        if (member.getLoginState().equals("solo")) {
            memberRepository.delete(member);
        }
    }

//    public String isMember(Member member){
//
//    }

    @Transactional
    public void deleteMemberByAccessToken(String accessToken) {
        Member member = findOptionalMemberByAccessToken(accessToken).orElseThrow(() -> new RuntimeException("Member not found"));
        Couple couple = coupleRepository.findCoupleById(member.getCoupleId());
        // delete chatting messages
        couple.getChattingRooms().forEach(chattingRoom -> chattingRoom.getMessageList().clear());
        // delete chatting rooms
        couple.getChattingRooms().clear();
        // delete couple
        coupleRepository.delete(couple);

//        List<Alert> alerts = member.getAlertList();
//
//        // delete all alerts related to member
//        alerts.forEach(alert -> alertRepository.delete(alert));


        memberRepository.deleteMemberByAccessToken(accessToken);

    }

    public Member SignUp(String accessToken, String refreshToken, String sub, Long expiresIn,
                         Boolean marketingConsent, String nickName, String fcmToken, String snsType) {

        Member newMember = new Member();
        newMember.setNickname(nickName);
        newMember.setState(null);
        newMember.setMarketingConsent(marketingConsent);
        newMember.setExpiresIn(expiresIn);
//                newMember.setOs(request.getOs()); //줄수있나?
        newMember.setSnsType(snsType);
        newMember.setFcmToken(fcmToken);
        newMember.setRefreshToken(refreshToken);
        newMember.setAccessToken(accessToken);
        newMember.setUniqueId(sub);
        newMember.setLoginState("solo");
        newMember.setEmoTitle("temptation");

        MemberConfig memberConfig = new MemberConfig();
        memberConfigRepository.save(memberConfig);
        newMember.setMemberConfig(memberConfig);

        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 6;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        Optional<Member> optionalMemberByGenerateCode = memberRepository.findOptionalMemberByInviteCode(generatedString);

        if (optionalMemberByGenerateCode.isPresent()) {
            random = new Random();

            generatedString = random.ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }
        newMember.setInviteCode(generatedString);

        //todo image process
        MemberImage MemberImage = new MemberImage();
        MemberImage.setImagePath("https://s3.ap-northeast-2.amazonaws.com/pillow.images/images/default.png");
        MemberImage.setFile_name("default");
        MemberImage.setUrl("https://s3.ap-northeast-2.amazonaws.com/pillow.images/images/default.png");
        newMember.setMemberImage(MemberImage);
        return memberRepository.save(newMember);
    }

    public String callbackAppleAndroid(String accessToken, String refreshToken, String sub, Long expiresIn, String state, String snsType) {
        Member newMember = new Member();

        newMember.setNickname(null);
        newMember.setState(state);
        newMember.setMarketingConsent(null);
        newMember.setExpiresIn(expiresIn);
//                newMember.setOs(request.getOs()); //줄수있나?
        newMember.setSnsType(snsType);
//                newMember.setFcmToken(request.getFcmToken());
        newMember.setRefreshToken(refreshToken);
        newMember.setAccessToken(accessToken);
        newMember.setUniqueId(sub);
        newMember.setLoginState("ready");
        newMember.setEmoTitle("temptation");

        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 6;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        Optional<Member> optionalMemberByGenerateCode = memberRepository.findOptionalMemberByInviteCode(generatedString);

        if (optionalMemberByGenerateCode.isPresent()) {
            random = new Random();

            generatedString = random.ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }
        newMember.setInviteCode(generatedString);

        //todo image process
        MemberImage MemberImage = new MemberImage();
        MemberImage.setImagePath("https://s3.ap-northeast-2.amazonaws.com/pillow.images/images/default.png");
        MemberImage.setFile_name("default");
        MemberImage.setUrl("https://s3.ap-northeast-2.amazonaws.com/pillow.images/images/default.png");
        newMember.setMemberImage(MemberImage);

        memberRepository.save(newMember);
        return "signUp";
    }

    public Member SignUpAppleAndroid(Member member, Boolean marketingConsent, String nickName, String fcmToken) {
        member.setNickname(nickName);
        member.setMarketingConsent(marketingConsent);
        member.setFcmToken(fcmToken);
        member.setNickname(nickName);
        member.setLoginState("solo");

        return memberRepository.save(member);

    }

    public void logout(String accessToken) throws RuntimeException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);
        member.setFcmToken(null);
        memberRepository.save(member);
        //accessToken만료시키기
    }

    public void setPassword(String accessToken, String password, String questionType, String answer) {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);
        member.getMemberConfig().setPassword(password);
        member.getMemberConfig().setLock(true);
        member.getMemberConfig().setQuestionType(questionType);
        member.getMemberConfig().setAnswer(answer);
        memberRepository.save(member);
    }

    public void updatePassword(String accessToken, String password) throws RuntimeException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);
        if (member.getMemberConfig().getLock()) {
            member.getMemberConfig().setPassword(password);
        } else {
            throw new RuntimeException("비번 초기 값이 없습니다.");

        }
        memberRepository.save(member);
    }

    public void unlockPassword(String accessToken) throws RuntimeException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);
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

        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);
        if (member.getMemberConfig().getLock()) {
            return member.getMemberConfig();
        } else {
            throw new RuntimeException("잠금상태가 아닙니다.");
        }


    }

    @Transactional
    public void deleteMyAccount(String accessToken) {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);
        Couple couple = coupleRepository.findCoupleById(member.getCoupleId());
        memberConfigRepository.delete(member.getMemberConfig());

        for (Challenge challenge : couple.getChallenges()) {
            challengeRepository.delete(challenge);
        }

        for (ChattingRoom chattingRoom : couple.getChattingRooms()) {
            chattingRoomRepository.delete(chattingRoom);
        }

        for (CoupleQuestion coupleQuestion : couple.getCoupleQuestions()) {
            coupleQuestionRepository.delete(coupleQuestion);
        }

        coupleRepository.delete(couple);

        memberRepository.delete(member);
    }

    public void checkPassword(String accessToken, String password) throws RuntimeException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);
        if (!password.equals(member.getMemberConfig().getPassword())) {
            throw new RuntimeException("비밀번호 틀림");
        }

    }

    public boolean validAnswer(String accessToken, String answer) throws RuntimeException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);
        //            throw new RuntimeException("답변 틀림");
        return answer.equals(member.getMemberConfig().getAnswer());

    }

    public void updateProfileDeaultImage(String accessToken, Long defaultProfile) {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);


        MemberImage MemberImage = new MemberImage();
        MemberImage.setImagePath("https://s3.ap-northeast-2.amazonaws.com/pillow.images/images/default" + defaultProfile + ".png");
        MemberImage.setFile_name("default");
        MemberImage.setUrl("https://s3.ap-northeast-2.amazonaws.com/pillow.images/images/default" + defaultProfile + ".png");

        member.setMemberImage(MemberImage);
        memberRepository.save(member);
    }

    public void updateProfileUserImage(String accessToken, MultipartFile userProfile) throws RuntimeException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);

        FileDetail fileDetail = fileUploadService.save(userProfile);
        MemberImage MemberImage = member.getMemberImage();

        MemberImage.setImagePath(fileDetail.getPath());
        MemberImage.setFile_name(fileDetail.getName());
        MemberImage.setUrl("https://s3.ap-northeast-2.amazonaws.com/" + "pillow.images/" + fileDetail.getPath());

        member.setMemberImage(MemberImage);
        memberRepository.save(member);
    }

    public Boolean checkNicknameChangeExceed(String accessToken) throws RuntimeException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);
        log.info(member.getNicknameChangeCount().toString());
        return member.getNicknameChangeCount() > 0;
    }

    @Transactional
    public void setNickname(String accessToken, String nickname) throws RuntimeException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(RuntimeException::new);
        if (member.getNicknameChangeCount() <= 0) {
            throw new RuntimeException("닉네임 변경 가능 횟수가 없습니다.");
        }
        boolean isValidUserId = Pattern.matches(REGEXP, nickname);
        if (!isValidUserId) {
            throw new RuntimeException("닉네임 형식 오류 입니다.");
        }
//

        member.setNickname(nickname);
        member.setNicknameChangeCount(member.getNicknameChangeCount() - 1);
        memberRepository.save(member);

    }


    public List<Member> findAll() {
        return memberRepository.findAll();
    }

}