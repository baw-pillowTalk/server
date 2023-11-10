package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.service.CoupleService;
import com.fgama.pillowtalk.service.MemberService;
import com.fgama.pillowtalk.service.QuestionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FcmController {
    private final CoupleService coupleService;
    private final MemberService memberService;
    private final QuestionService questionService;

    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @PostMapping("api/fcm/emotion")
    public EmotionResponse requestFcm(@RequestBody EmotionRequest request) {
        Member self = memberService.getCurrentMember();
        Couple couple = coupleService.getCouple(self);

        Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();
        log.info("응답 act = " + request.getAccessToken());
        log.info("응답 Fcm = " + self.getFcmToken());
        log.info("파트너 fcm = " + partner.getFcmToken());
        String fcmDetail = firebaseCloudMessageService.getFcmJsonObject(
                "오늘 내 상태는 말야!\uD83D\uDC8B",
                "emotionRequest",
                self.getNickname() + "님이 시그널을 보냈어요!");


        return new EmotionResponse(firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken()));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class EmotionRequest {
        private String accessToken;
        private String question;
        private String answer;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TradeRequest {
        private String accessToken;
        private String TempKey;
    }

    @Data
    @AllArgsConstructor
    static class EmotionResponse {
        private String status;
    }
}