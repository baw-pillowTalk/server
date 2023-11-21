package com.fgama.pillowtalk.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgama.pillowtalk.components.AmazonS3ResourceStorage;
import com.fgama.pillowtalk.constant.HttpResponse;
import com.fgama.pillowtalk.domain.chattingMessage.ChattingMessage;
import com.fgama.pillowtalk.domain.CoupleChallenge;
import com.fgama.pillowtalk.domain.CoupleQuestion;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.dto.member.UpdateMySignalRequestDto;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.service.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChattingMessageController {
    private final ChattingRoomService chattingRoomService;
    private final CoupleQuestionService coupleQuestionService;
    private final ChallengeService challengeService;
    private final ChattingMessageService chattingMessageService;
    private final MemberService memberService;
    private final CoupleService coupleService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    private final AmazonS3ResourceStorage amazonS3ResourceStorage;

    /***
     * 채팅 로직
     */

    @PostMapping("/api/v1/chatting-message/text")
    public JSendResponse sendTextMessage(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestBody TextChattingRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());

            ChattingMessage chattingMessage = chattingRoomService.addTextChattingMessage(request.getMessage());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", chattingMessage.getNumber());
            jsonObject.put("isRead", chattingMessage.getIsRead());
            jsonObject.put("createAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            throw e;
//            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @PostMapping("/api/v1/chatting-message/question")
    public JSendResponse shareQuestion(@RequestHeader("Authorization") String authorizationHeader,
                                       @RequestBody QuestionChattingRequest request) {

        String accessToken = authorizationHeader.substring("Bearer ".length());
        //db
        ChattingMessage chattingMessage = chattingRoomService.addQuestionChattingMessage(request.getIndex());
        //fcm
        CoupleQuestion coupleQuestion = coupleQuestionService.findByIndex(Math.toIntExact(request.getIndex()));
        //response
        JSONObject coupleQuestionObject = new JSONObject();

        coupleQuestionObject.put("index", coupleQuestion.getNumber());
        coupleQuestionObject.put("title", coupleQuestion.getQuestion().getTitle());
        coupleQuestionObject.put("selfAnswer", coupleQuestion.getSelfAnswer());
        coupleQuestionObject.put("partnerAnswer", coupleQuestion.getPartnerAnswer());
        coupleQuestionObject.put("createAt", coupleQuestion.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("coupleQuestion", coupleQuestionObject);
        jsonObject.put("chatSender", chattingMessage.getMember().getNickname());
        jsonObject.put("index", chattingMessage.getNumber());
        jsonObject.put("pageNo", chattingMessage.getNumber() / 4);
        jsonObject.put("isRead", chattingMessage.getIsRead());
        jsonObject.put("createAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);

    }

    @PostMapping("/api/v1/chatting-message/challenge")
    public JSendResponse shareChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody ChallengeChattingRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            //db
            ChattingMessage chattingMessage = chattingRoomService.addChallengeChattingMessage(request.getIndex());
            //fcm

            CoupleChallenge coupleChallengeServiceByIndex = challengeService.findByIndex(accessToken, Math.toIntExact(request.getIndex()));
            //response
            JSONObject coupleChallengeObject = new JSONObject();

            coupleChallengeObject.put("index", coupleChallengeServiceByIndex.getNumber());
//            coupleChallengeObject.put("category",challengeServiceByIndex.getCategory());
            coupleChallengeObject.put("challengeTitle", coupleChallengeServiceByIndex.getTitle());
            coupleChallengeObject.put("challengeBody", coupleChallengeServiceByIndex.getBody());
            coupleChallengeObject.put("deadline", coupleChallengeServiceByIndex.getTargetDate());
            coupleChallengeObject.put("creator", memberService.getCurrentMember().getNickname());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("coupleChallenge", coupleChallengeObject);
            jsonObject.put("index", chattingMessage.getNumber());
            jsonObject.put("isRead", chattingMessage.getIsRead());
            jsonObject.put("createAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }
    }

    @PostMapping("/api/v1/chatting-message/image")
    public JSendResponse sendImage(@RequestHeader("Authorization") String authorizationHeader,
                                   ImageChattingRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            //db
            ChattingMessage chattingMessage = chattingRoomService.addImageChattingMessage(request.getImageFile());
            //fcm
            Member partner = memberService.getPartnerByAccessToken(accessToken);
            //response
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", chattingMessage.getNumber());
            jsonObject.put("isRead", chattingMessage.getIsRead());
            jsonObject.put("createAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            log.error("error :", e);
            return new JSendResponse(HttpResponse.HTTP_FAIL, "image 채팅 api 실패 " + e);
        }
    }


    @PostMapping("/api/v1/chatting-message/voice")
    public JSendResponse sendVoice(@RequestHeader("Authorization") String authorizationHeader,
                                   VoiceChattingRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            //db
            ChattingMessage chattingMessage = chattingRoomService.addVoiceChattingMessage(request.getVoiceFile());
            //fcm
            Member partner = memberService.getPartnerByAccessToken(accessToken);
            //response
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", chattingMessage.getNumber());
            jsonObject.put("isRead", chattingMessage.getIsRead());
            jsonObject.put("createAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            log.error("error :", e);
            return new JSendResponse(HttpResponse.HTTP_FAIL, "voice 채팅 api 실패 " + e);
        }
    }
    @PostMapping("/api/v1/chatting-message/signal")
    public JSendResponse sendSignal(@Valid @RequestBody UpdateMySignalRequestDto request) {
        try {
            this.memberService.updateMemberSignal(request);
            ChattingMessage chattingMessage = chattingRoomService.addSignalChattingMessage(request.getMySignal());
            //response
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", chattingMessage.getNumber());
            jsonObject.put("isRead", chattingMessage.getIsRead());
            jsonObject.put("createAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            log.error("error :", e);
            return new JSendResponse(HttpResponse.HTTP_FAIL, "signal 채팅 api 실패 " + e);
        }
    }

    @PostMapping("/api/v1/chatting-message/reset-partner-password")
    public JSendResponse sendResetPartnerPassword(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            //db
            ChattingMessage chattingMessage = chattingRoomService.addResetPasswordChattingMessage();
            //fcm
            Member partner = memberService.getPartnerByAccessToken(accessToken);

            //response
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", chattingMessage.getNumber());
            jsonObject.put("isRead", chattingMessage.getIsRead());
            jsonObject.put("createAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            log.error("error :", e);
            return new JSendResponse(HttpResponse.HTTP_FAIL, "reset 채팅 api 실패 " + e);
        }
    }

    /***
     * 파트너가 마지막에 보낸 채팅 (미리보기용)
     * @param authorizationHeader
     * @return
     */
    @GetMapping("/api/v1/chatting-message/partner/last")
    public JSendResponse getPartnerLastMessage(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            String message = memberService.findPartnerLastMessage(accessToken);
            Boolean isRead = memberService.IsPartnerLastMessageRead(accessToken);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", message);
            jsonObject.put("isRead", isRead);
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, e.toString());
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TextChattingRequest {
        private String message;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class QuestionChattingRequest {
        private Long index;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class EmojiChattingRequest {
        private String emoji;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class VoiceChattingRequest {
        private MultipartFile voiceFile;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ResetPartnerPasswordChattingRequest {
        private MultipartFile voiceFile;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ImageChattingRequest {
        private MultipartFile imageFile;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ChallengeChattingRequest {
        private int index;
    }

}