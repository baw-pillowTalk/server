package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.config.Constants;
import com.fgama.pillowtalk.domain.ChattingMessage;
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

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChattingRoomApiV1 {
    private final ChattingRoomService chattingRoomService;
    private final ChattingMessageService chattingMessageService;
    private final CoupleService coupleService;
    private final CoupleQuestionService coupleQuestionService;
    private final MemberService memberService;
    private final QuestionService questionService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @PostMapping("/api/v1/chatting-messages")
    public JSendResponse loadChatList(@RequestHeader("Authorization") String authorizationHeader,
                                      @RequestBody ChattingMessageRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            List<ChattingMessage> chattingMessages = chattingMessageService.loadChatList(accessToken, request.getPageNo());
            Member member = memberService.findMemberByAccessToken(accessToken);
            JSONArray jsonArray = new JSONArray();

            for (ChattingMessage message : chattingMessages) {
                JSONObject coupleQuestionJsonObject = new JSONObject();
//                if (message.getType().equals("question")) {
//                    CoupleQuestion coupleQuestion = coupleQuestionService.findByIndex(accessToken, Math.toIntExact(message.getQuestionIndex()));
//                    coupleQuestionJsonObject.put("title", coupleQuestion.getQuestion().getTitle());
//                    coupleQuestionJsonObject.put("header", coupleQuestion.getQuestion().getHeader());
//                    coupleQuestionJsonObject.put("body", coupleQuestion.getQuestion().getBody());
//                    coupleQuestionJsonObject.put("partnerAnswer", coupleQuestion.getPartnerAnswer());
//                    coupleQuestionJsonObject.put("myAnswer", coupleQuestion.getSelfAnswer());
//                    coupleQuestionJsonObject.put("createAt", coupleQuestion.getCreatedAt());
//                    coupleQuestionJsonObject.put("index", coupleQuestion.getNumber());
//                }

                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("index", message.getNumber());
                jsonMessage.put("message", message.getMessage());
                jsonMessage.put("url", message.getResourceUrl());
//                jsonMessage.put("coupleQuestion", coupleQuestionJsonObject);
                jsonMessage.put("coupleQuestion", message.getQuestionIndex());
                jsonMessage.put("coupleChallenge", message.getChallengeIndex());
                jsonMessage.put("emoji", message.getEmoji());
                jsonMessage.put("type", message.getType());
                jsonMessage.put("createAt", message.getCreatedAt());
                jsonMessage.put("chatSender", message.getMember().getNickname());
                jsonMessage.put("mine", message.getMember().getNickname().equals(member.getNickname()));
                jsonMessage.put("isRead", message.getIsRead());
                jsonArray.add(jsonMessage);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("page", request.getPageNo());
            jsonObject.put("chatting", jsonArray);

            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
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

            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_SUCCESS, e.getMessage());
        }


    }

    @Data
    static class ChattingMessageRequest {
        private int pageNo;
    }
}