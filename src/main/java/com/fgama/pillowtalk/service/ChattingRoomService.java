package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.components.FileDetail;
import com.fgama.pillowtalk.domain.*;
import com.fgama.pillowtalk.domain.chattingMessage.*;
import com.fgama.pillowtalk.dto.member.GetPartnerInfoResponseDto;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.repository.ChallengeRepository;
import com.fgama.pillowtalk.repository.ChattingMessageRepository;
import com.fgama.pillowtalk.repository.ChattingRoomRepository;
import com.fgama.pillowtalk.repository.CoupleQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChattingRoomService {
    private final ChattingRoomRepository chattingRoomRepository;
    private final ChattingMessageRepository chattingMessageRepository;
    private final CoupleQuestionRepository coupleQuestionRepository;
    private final ChallengeRepository challengeRepository;
    private final FileUploadService fileUploadService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;
    private final MemberService memberService;
    private final CoupleService coupleService;

    public Long join(ChattingRoom chattingRoom) {
//        validateDuplicateChattingRoom(chattingRoom);
        ChattingRoom chattingRoom1 = chattingRoomRepository.save(chattingRoom);
        return chattingRoom1.getId();
    }

    public void validateDuplicateChattingRoom(ChattingRoom chattingRoom) {
        List<ChattingRoom> findChattingRooms = chattingRoomRepository.findChattingRoomsByTitle(chattingRoom.getTitle());
        if (!findChattingRooms.isEmpty()) {
            throw new IllegalStateException("이미 존재 하는 채팅방 입니다");
        }
    }

    public List<ChattingRoom> findChattingRoomsByCoupleId(Long coupleId) {
        return chattingRoomRepository.findChattingRoomsByCoupleId(coupleId);
    }

    public ChattingRoom findChattingRoomsByCoupleIdAndTitle(Long coupleId, String title) {
        return chattingRoomRepository.findChattingRoomsByCoupleIdAndTitle(coupleId, title);
    }

    public int countByCoupleId(Long coupleId) throws IllegalStateException {
        int count = chattingRoomRepository.countByCoupleId(coupleId);
        if (count == 0) {
            throw new IllegalStateException("질문을 가지고있지 않습니다");
        }

        return count;
    }


    public Page<ChattingRoom> findNewest(Long coupleId, int pageNo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageNo, 4, sort);
        Page<ChattingRoom> rooms = chattingRoomRepository.findChattingRoomsByCoupleId(coupleId, pageRequest);

        return rooms;
    }

    public ChattingMessage addQuestionChattingMessage(Long questionIndex) throws NullPointerException {
        Member self = memberService.getCurrentMember();
        Couple couple = this.coupleService.getCouple(self);
        Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();

        QuestionChattingMessage chattingMessage = QuestionChattingMessage.builder()
                .questionIndex(questionIndex)
                .self(self)
                .isRead(partner.getChattingRoomStatus())
                .chattingRoom(couple.getChattingRoom())
                .number((long) couple.getChattingRoom().getMessageList().size())
                .build();

        saveChattingMessage(couple, chattingMessage);
        CoupleQuestion coupleQuestion = coupleQuestionRepository.findByNumberAndCoupleId(Math.toIntExact(questionIndex), couple.getId());

        String fcmDetail = firebaseCloudMessageService.questionMessageFcmJsonObject(chattingMessage, coupleQuestion);
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

        return chattingMessage;
    }

    public ChattingMessage addCompleteChallengeChattingMessage(int challengeIndex) throws NullPointerException {
        Member self = memberService.getCurrentMember();
        Couple couple = this.coupleService.getCouple(self);
        Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();
        CompleteChallengeChattingMessage chattingMessage = CompleteChallengeChattingMessage.builder()
                .challengeIndex(challengeIndex)
                .self(self)
                .isRead(partner.getChattingRoomStatus())
                .chattingRoom(couple.getChattingRoom())
                .number((long) couple.getChattingRoom().getMessageList().size())
                .build();

        saveChattingMessage(couple, chattingMessage);
        CoupleChallenge challenge = challengeRepository.findByCoupleIdAndNumber(couple.getId(), challengeIndex);
        String fcmDetail = firebaseCloudMessageService.completeChallengeMessageFcmJsonObject(chattingMessage, challenge);
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

        return chattingMessage;
    }

    public ChattingMessage addChallengeChattingMessage(int challengeIndex) throws NullPointerException {
        Member self = memberService.getCurrentMember();
        Couple couple = this.coupleService.getCouple(self);
        Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();
        ChallengeChattingMessage chattingMessage = ChallengeChattingMessage.builder()
                .challengeIndex(challengeIndex)
                .self(self)
                .isRead(partner.getChattingRoomStatus())
                .chattingRoom(couple.getChattingRoom())
                .number((long) couple.getChattingRoom().getMessageList().size())
                .build();

        saveChattingMessage(couple, chattingMessage);
        CoupleChallenge challenge = challengeRepository.findByCoupleIdAndNumber(couple.getId(), challengeIndex);
        String fcmDetail = firebaseCloudMessageService.challengeMessageFcmJsonObject(chattingMessage, challenge);
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

        return chattingMessage;
    }

    public ChattingMessage addTextChattingMessage(String textMessage) throws NullPointerException {
        Member self = memberService.getCurrentMember();
        Couple couple = memberService.getCouple(self);
        Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();

        TextChattingMessage chattingMessage = TextChattingMessage.builder()
                .message(textMessage)
                .self(self)
                .isRead(partner.getChattingRoomStatus())
                .chattingRoom(couple.getChattingRoom())
                .number((long) couple.getChattingRoom().getMessageList().size())
                .build();

        saveChattingMessage(couple, chattingMessage);

        String fcmDetail = firebaseCloudMessageService.textMessageFcmJsonObject(chattingMessage);
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

        return chattingMessage;
    }

    public ChattingMessage addImageChattingMessage(MultipartFile imageFile) throws NullPointerException {
        Member self = memberService.getCurrentMember();
        Couple couple = this.coupleService.getCouple(self);
        Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();

        FileDetail fileDetail = fileUploadService.save(imageFile);
        String url = "https://s3.ap-northeast-2.amazonaws.com/" + "pillow.images/" + fileDetail.getPath();

        ImageChattingMessage chattingMessage = ImageChattingMessage.builder()
                .resourceUrl(url)
                .self(self)
                .isRead(partner.getChattingRoomStatus())
                .chattingRoom(couple.getChattingRoom())
                .number((long) couple.getChattingRoom().getMessageList().size())
                .build();

        saveChattingMessage(couple, chattingMessage);

        String fcmDetail = firebaseCloudMessageService.imageMessageFcmJsonObject(chattingMessage, url);
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

        return chattingMessage;
    }

    public ChattingMessage addVoiceChattingMessage(MultipartFile voiceFile) throws NullPointerException {
        Member self = memberService.getCurrentMember();
        Couple couple = this.coupleService.getCouple(self);
        Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();

        FileDetail fileDetail = fileUploadService.save(voiceFile);
        //todo 스토리지 바꾸기
        String url = "https://s3.ap-northeast-2.amazonaws.com/" + "pillow.images/" + fileDetail.getPath();

        VoiceChattingMessage chattingMessage = VoiceChattingMessage.builder()
                .resourceUrl(url)
                .self(self)
                .isRead(partner.getChattingRoomStatus())
                .chattingRoom(couple.getChattingRoom())
                .number((long) couple.getChattingRoom().getMessageList().size())
                .build();

        saveChattingMessage(couple, chattingMessage);

        String fcmDetail = firebaseCloudMessageService.voiceMessageFcmJsonObject(chattingMessage, url);
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

        return chattingMessage;
    }

    public ChattingMessage addSignalChattingMessage(Integer signalPercent) throws NullPointerException {
        Member self = memberService.getCurrentMember();
        Couple couple = this.coupleService.getCouple(self);
        Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();

        SignalChattingMessage chattingMessage = SignalChattingMessage.builder()
                .signalPercent(signalPercent)
                .self(self)
                .isRead(partner.getChattingRoomStatus())
                .chattingRoom(couple.getChattingRoom())
                .number((long) couple.getChattingRoom().getMessageList().size())
                .build();

        saveChattingMessage(couple, chattingMessage);

        String fcmDetail = firebaseCloudMessageService.signalMessageFcmJsonObject(chattingMessage);
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

        return chattingMessage;
    }

    public ChattingMessage addResetPasswordChattingMessage() throws NullPointerException {
        Member self = memberService.getCurrentMember();
        Couple couple = this.coupleService.getCouple(self);
        Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();

        ResetPasswordChattingMessage chattingMessage = ResetPasswordChattingMessage.builder()
                .self(self)
                .isRead(partner.getChattingRoomStatus())
                .chattingRoom(couple.getChattingRoom())
                .number((long) couple.getChattingRoom().getMessageList().size())
                .build();

        saveChattingMessage(couple, chattingMessage);

        String fcmDetail = firebaseCloudMessageService.resetPartnerPasswordMessageFcmJsonObject(chattingMessage);
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

        return chattingMessage;
    }

    public ChattingMessage addPressForAnswerChattingMessage() throws NullPointerException {
        Member self = memberService.getCurrentMember();
        Couple couple = this.coupleService.getCouple(self);
        Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();

        PressForAnswerChattingMessage chattingMessage = PressForAnswerChattingMessage.builder()
                .self(self)
                .isRead(partner.getChattingRoomStatus())
                .chattingRoom(couple.getChattingRoom())
                .number((long) couple.getChattingRoom().getMessageList().size())
                .build();

        saveChattingMessage(couple, chattingMessage);

        String fcmDetail = firebaseCloudMessageService.pressForAnswerMessageFcmJsonObject(chattingMessage);
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

        return chattingMessage;
    }

    private ChattingRoom saveChattingMessage(Couple couple, ChattingMessage chattingMessage) {
        ChattingRoom chattingRoom = chattingRoomRepository.findChattingRoomsByCoupleId(couple.getId()).get(0);
        chattingRoom.addMessage(chattingMessage);
        return chattingRoomRepository.save(chattingRoom);
    }

    public boolean resetPartnerPassword(int chattingMessageIndex) {
        Member self = memberService.getCurrentMember();
        Couple couple = this.coupleService.getCouple(self);

        ChattingRoom chattingRoom = chattingRoomRepository.findChattingRoomsByCoupleId(couple.getId()).get(0);
        if (chattingRoom.getMessageList().get(chattingMessageIndex) instanceof ResetPasswordChattingMessage) {
            ResetPasswordChattingMessage resetPasswordChattingMessage = (ResetPasswordChattingMessage) chattingRoom.getMessageList().get(chattingMessageIndex);
            if (resetPasswordChattingMessage.isPasswordResetExpired() == true) {
                return false;
            }

        }
        List<ChattingMessage> messageList = chattingRoom.getMessageList();
        for (ChattingMessage chattingMessage : messageList) {
            if (chattingMessage instanceof ResetPasswordChattingMessage) {
                ResetPasswordChattingMessage resetPasswordChattingMessage = (ResetPasswordChattingMessage) chattingMessage;
                if (resetPasswordChattingMessage.isPasswordResetExpired() == false) {
                    resetPasswordChattingMessage.changePasswordResetExpired();
                }
            }

        }
        return true;

    }

    public Member getMember() {
        return this.memberService.getCurrentMember();
    }
}