package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.ChattingMessage;
import com.fgama.pillowtalk.domain.ChattingRoom;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.exception.MemberNotFoundException;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.repository.ChattingMessageRepository;
import com.fgama.pillowtalk.repository.ChattingRoomRepository;
import com.fgama.pillowtalk.repository.CoupleRepository;
import com.fgama.pillowtalk.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CoupleService {
    private final CoupleRepository coupleRepository;
    private final ChattingRoomRepository chattingRoomRepository;
    private final ChattingMessageRepository chattingMessageRepository;
    private final MemberRepository memberRepository;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    public Long join(Couple couple) {
        Couple couple1 = coupleRepository.save(couple);
        return couple1.getId();
    }

    public Couple createCouple(Member self, Member partner) throws RuntimeException {
        if (isCouple(self, partner)) {
            throw new RuntimeException("커플이 이미 존재합니다.");
        }
        Couple couple = new Couple();
        couple.setSelf(self);
        couple.setPartner(partner);
        couple.setCreatedAt(LocalDateTime.now());
        //        couple.setStartDate();
        couple.setCoupleQuestions(new ArrayList<>());
        couple.setChallenges(new ArrayList<>());
        couple.setChattingRooms(new ArrayList<>());
        couple.setStatus("available");
        Couple save = coupleRepository.save(couple);

        ChattingRoom chattingRoom = new ChattingRoom();
        chattingRoom.setMessageList(new ArrayList<>());
        chattingRoom.setTitle("커플 채팅방");
        chattingRoom.setCreatedAt(LocalDateTime.now());
        chattingRoom.setCouple(couple);
        chattingRoomRepository.save(chattingRoom);

        self.setCoupleId(save.getId());
        self.setLoginState("couple");
        partner.setCoupleId(save.getId());
        partner.setLoginState("couple");

        memberRepository.save(self);
        memberRepository.save(partner);

        String fcmDetail = firebaseCloudMessageService.getFcmJsonObject("필로우에 오신걸 환영해요\uD83D\uDD13", "createCouple", "두 분의 커플 매칭이 완료 되었어요!");
        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

        return save;
    }

    private Boolean isCouple(Member self, Member partner) {
        if (self.getLoginState().equals("couple")) {
            return true;
        }

        if (partner.getLoginState().equals("couple")) {
            return true;
        }

        if (self.getCoupleId() != null) {
            return true;
        }

        return partner.getCoupleId() != null;
    }

    public String getMyProfileImageUrl(String accessToken) throws NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(() -> new MemberNotFoundException());
        return member.getMemberImage().getUrl();

    }


    public String getPartnerProfileImageUrl(String accessToken) throws NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(() -> new MemberNotFoundException());
        Couple coupleById = coupleRepository.findCoupleById(member.getCoupleId());
        Member partner = coupleById.getSelf() == member ? coupleById.getPartner() : member;
        return partner.getMemberImage().getUrl();

    }


    public Couple findCoupleById(Long id) {
        return coupleRepository.findCoupleById(id);
    }

    public Couple findCoupleByMember(Member member) {
        return coupleRepository.findCoupleById(member.getCoupleId());
    }

    public Couple findCoupleByCoupleCode(String coupleCode) {
        return coupleRepository.findCoupleByCoupleCode(coupleCode);
    }

    public String findCoupleCodeByMember(Member member) throws RuntimeException {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        Couple couple = coupleRepository.findCoupleById(member.getCoupleId());
        if (couple == null) {
            throw new RuntimeException("Couple not found");
        }

        String coupleCode = couple.getCoupleCode();
        if (coupleCode == null) {
            throw new IllegalStateException("Couple code is null");
        }

        return coupleCode;
    }

    public List<Couple> findCoupleAll() {
        return coupleRepository.findAll();
    }

    public void deleteCoupleById(Long id) {
        coupleRepository.deleteById(id);
    }

    public void removeCoupleById(Long id) {
        Couple couple = coupleRepository.findCoupleById(id);
        List<ChattingRoom> rooms = chattingRoomRepository.findChattingRoomsByCoupleId(couple.getId());
        for (ChattingRoom chattingRoom : rooms) {
            List<ChattingMessage> chattingMessages = chattingMessageRepository.findByChattingRoom(chattingRoom);
            for (ChattingMessage chattingMessage : chattingMessages) {
                chattingMessageRepository.delete(chattingMessage);
            }
            chattingRoomRepository.delete(chattingRoom);
        }


        coupleRepository.deleteById(id);
    }

    public void deleteCoupleByCoupleCode(String coupleCode) {
        coupleRepository.deleteCoupleByCoupleCode(coupleCode);
    }

}