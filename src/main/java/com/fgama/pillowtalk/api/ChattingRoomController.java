package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.constant.HttpResponse;
import com.fgama.pillowtalk.domain.CoupleChallenge;
import com.fgama.pillowtalk.domain.chattingMessage.*;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChattingRoomController {
    private final ChattingRoomService chattingRoomService;
    private final ChattingMessageService chattingMessageService;
    private final CoupleService coupleService;
    private final CoupleQuestionService coupleQuestionService;
    private final MemberService memberService;
    private final QuestionService questionService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;
    private final ChallengeService challengeService;
    @PostMapping("/api/v1/chatting-messages")
    public JSendResponse loadChatList(@RequestHeader("Authorization") String authorizationHeader,
                                      @RequestBody ChattingMessageRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            List<ChattingMessage> chattingMessages = chattingMessageService.loadChatList(accessToken, request.getPageNo());
            Member member = memberService.getCurrentMember();
            JSONArray jsonArray = new JSONArray();

            for (ChattingMessage message : chattingMessages) {
                JSONObject coupleQuestionJsonObject = new JSONObject();
                if (message instanceof SignalChattingMessage) {
                    SignalChattingMessage signalMessage = (SignalChattingMessage) message;
                    jsonArray.add(signalMessage.getJSONObject(member));
                } else if (message instanceof TextChattingMessage) {
                    TextChattingMessage textMessage = (TextChattingMessage) message;
                    jsonArray.add(textMessage.getJSONObject(member));
                } else if (message instanceof QuestionChattingMessage) {
                    QuestionChattingMessage questionMessage = (QuestionChattingMessage) message;
                    jsonArray.add(questionMessage.getJSONObject(member));
                } else if (message instanceof ChallengeChattingMessage) {
                    ChallengeChattingMessage challengeMessage = (ChallengeChattingMessage) message;
                    CoupleChallenge challenge = challengeService.findByNumber(challengeMessage.getChallengeIndex());
                    JSONObject jsonMessage = new JSONObject();

                    JSONObject coupleChallengeObject = new JSONObject();
                    coupleChallengeObject.put("index",challenge.getNumber());
                    coupleChallengeObject.put("challengeTitle",challenge.getTitle());
                    coupleChallengeObject.put("challengeBody",challenge.getBody());
                    coupleChallengeObject.put("deadline",challenge.getTargetDate());
                    coupleChallengeObject.put("creator",challenge.getCreator());


                    jsonMessage.put("coupleChallenge", coupleChallengeObject);
                    jsonMessage.put("index", challengeMessage.getNumber());
                    jsonMessage.put("type", challengeMessage.getMessageType());
                    jsonMessage.put("createAt", challengeMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                    jsonMessage.put("chatSender", challengeMessage.getMember().getNickname());
                    jsonMessage.put("mine", challengeMessage.getMember().getNickname().equals(member.getNickname()));
                    jsonMessage.put("isRead", challengeMessage.getIsRead());
                    jsonArray.add(jsonMessage);
                } else if (message instanceof ImageChattingMessage) {
                    ImageChattingMessage imageMessage = (ImageChattingMessage) message;
                    jsonArray.add(imageMessage.getJSONObject(member));
                } else if (message instanceof VoiceChattingMessage) {
                    VoiceChattingMessage voiceMessage = (VoiceChattingMessage) message;
                    jsonArray.add(voiceMessage.getJSONObject(member));
                } else if (message instanceof ResetPasswordChattingMessage) {
                    ResetPasswordChattingMessage resetPasswordMessage = (ResetPasswordChattingMessage) message;
                    jsonArray.add(resetPasswordMessage.getJSONObject(member));
                } else if (message instanceof CompleteChallengeChattingMessage) {
                    CompleteChallengeChattingMessage completeChallengeChattingMessage = (CompleteChallengeChattingMessage) message;
                    CoupleChallenge challenge = challengeService.findByNumber(completeChallengeChattingMessage.getChallengeIndex());
                    JSONObject jsonMessage = new JSONObject();

                    JSONObject coupleChallengeObject = new JSONObject();
                    coupleChallengeObject.put("index",challenge.getNumber());
                    coupleChallengeObject.put("challengeTitle",challenge.getTitle());
                    coupleChallengeObject.put("challengeBody",challenge.getBody());
                    coupleChallengeObject.put("deadline",challenge.getTargetDate());
                    coupleChallengeObject.put("creator",challenge.getCreator());


                    jsonMessage.put("coupleChallenge", coupleChallengeObject);
                    jsonMessage.put("index", completeChallengeChattingMessage.getNumber());
                    jsonMessage.put("type", completeChallengeChattingMessage.getMessageType());
                    jsonMessage.put("createAt", completeChallengeChattingMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                    jsonMessage.put("chatSender", completeChallengeChattingMessage.getMember().getNickname());
                    jsonMessage.put("mine", completeChallengeChattingMessage.getMember().getNickname().equals(member.getNickname()));
                    jsonMessage.put("isRead", completeChallengeChattingMessage.getIsRead());
                    jsonArray.add(jsonMessage);
                }else if (message instanceof ChaseUpChattingMessage) {
                    ChaseUpChattingMessage chaseUpChattingMessage = (ChaseUpChattingMessage) message;
                    jsonArray.add(chaseUpChattingMessage.getJSONObject(member));
                }else if (message instanceof PressForAnswerChattingMessage) {
                    PressForAnswerChattingMessage pressForAnswerChattingMessage = (PressForAnswerChattingMessage) message;
                    jsonArray.add(pressForAnswerChattingMessage.getJSONObject(member));
                }
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("page", request.getPageNo());
            jsonObject.put("chatting", jsonArray);

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            throw e;
//            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }


    }

    @GetMapping("/api/v1/chatting-message/last/page-no")
    public JSendResponse getLatestChatPageNo(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pageNo", chattingMessageService.getLatestChatPageNo(accessToken));

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, e.getMessage());
        }


    }
   @PostMapping("/api/v1/chatting-room/reset-password")
    public JSendResponse resetPartnerPassword(@RequestHeader("Authorization") String authorizationHeader,
                                              @RequestBody ChattingMessageIndexRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());

            chattingRoomService.resetPartnerPassword(request.getChattingMessageIndex());

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null);
        } catch (NullPointerException e) {
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, e.getMessage());
        }


    }

    @Data
    static class ChattingMessageRequest {
        private int pageNo;
    } @Data
    static class ChattingMessageIndexRequest{
        private int chattingMessageIndex;
    }
}
