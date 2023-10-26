package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.domain.*;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChattingRoomApiTest {
    @Autowired
    ChattingRoomService chattingRoomService;
    @Autowired
    QuestionService questionService;
    @Autowired
    CoupleService coupleService;
    @Autowired
    FirebaseCloudMessageService firebaseCloudMessageService;
    @Autowired
    AlertService alertService;
    @Autowired
    MemberService memberService;
    @Test
    void questionUpdate() {
        Member member = memberService.findMemberByEmail("dragon041414@gmail.com");
        Couple couple = coupleService.findCoupleById(member.getCoupleId());
        List<ChattingRoom> chattingRoomsByCoupleId = chattingRoomService.findChattingRoomsByCoupleId(couple.getId());

        List<Question> all = questionService.findAll();


        if (chattingRoomsByCoupleId.isEmpty()) {
            ChattingRoom chattingRoom = new ChattingRoom();
            chattingRoom.setMessageList(new ArrayList<>());
            chattingRoom.setTitle(all.get(0).getBody());
            chattingRoom.setCreatedAt(LocalDateTime.now());
            chattingRoom.setCouple(couple);
            chattingRoomService.join(chattingRoom);


            String fcmDetail = firebaseCloudMessageService.getQuestionFcmJsonObject(
                    "자기야 내 취향은 있잖아\uD83D\uDC93",
                    "newQuestion",
                    "질문에 답변을 남기고 서로를 알아가보세요!",
                    all.get(0).getTitle());

            String partnerFcmToken = couple.getPartner().getFcmToken();
            firebaseCloudMessageService.sendFcmMessage(fcmDetail, partnerFcmToken);

            String selfFcmToken = couple.getSelf().getFcmToken();
            firebaseCloudMessageService.sendFcmMessage(fcmDetail, selfFcmToken);

            alertService.join(new Alert(couple.getSelf(), LocalDateTime.now(), "1번째 질문이 도착했습니다."));
            alertService.join(new Alert(couple.getPartner(), LocalDateTime.now(), "1번째 질문이 도착했습니다."));


        } else {
            if (all.size() == chattingRoomsByCoupleId.size()) {

                return;
            }

            for (Question findQuestion : all) {
                boolean check = false;
                for (ChattingRoom findChattingRoom : chattingRoomsByCoupleId) {
                    if (findQuestion.getTitle().equals(findChattingRoom.getTitle())) {
                        check = true;
                        break;
                    }
                }

                if (!check) {
                    String partnerFcmToken = couple.getPartner().getFcmToken();

                    ChattingRoom chattingRoom = new ChattingRoom();
                    chattingRoom.setMessageList(new ArrayList<>());
                    chattingRoom.setTitle(findQuestion.getTitle());
                    chattingRoom.setCreatedAt(LocalDateTime.now());
                    chattingRoom.setCouple(couple);
                    chattingRoomService.join(chattingRoom);

                    String fcmDetail = firebaseCloudMessageService.getQuestionFcmJsonObject(
                            "자기야 내 취향은 있잖아\uD83D\uDC93",
                            "newQuestion",
                            "질문에 답변을 남기고 서로를 알아가보세요!",
                            findQuestion.getTitle());

                    firebaseCloudMessageService.sendFcmMessage(fcmDetail, partnerFcmToken);

                    String selfFcmToken = couple.getSelf().getFcmToken();
                    firebaseCloudMessageService.sendFcmMessage(fcmDetail, selfFcmToken);

                    alertService.join(new Alert(couple.getPartner(), LocalDateTime.now(), (findQuestion.getNumber() + 1) + "번째 질문이 도착했습니다."));
                    alertService.join(new Alert(couple.getSelf(), LocalDateTime.now(), (findQuestion.getNumber() + 1) + "번째 질문이 도착했습니다."));

                    break;
                }
            }
        }
    }

}