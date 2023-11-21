package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.constant.HttpResponse;
import com.fgama.pillowtalk.domain.*;
import com.fgama.pillowtalk.domain.chattingMessage.ChattingMessage;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CoupleQuestionController {
    private final CoupleService coupleService;
    private final MemberService memberService;
    private final ChattingRoomService chattingRoomService;
    private final QuestionService questionService;
    private final QuestionCharService questionCharService;
    private final CoupleQuestionService coupleQuestionService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @GetMapping("/api/v1/couple-question/last/page-no")
    public JSendResponse getLatestChatPageNo(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pageNo", coupleQuestionService.getLatestQuestionPageNo(accessToken));

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.getMessage());
        }
    }


//    @PostMapping("/api/ v1/couple-question")
//    public JSendResponse createCoupleQuestion(@RequestHeader("Authorization") String authorizationHeader,
//                                              @RequestBody request) {
//        try {
//            String accessToken = authorizationHeader.substring("Bearer ".length());
//            //db
//            ChattingMessage chattingMessage = chattingRoomService.addChattingMessage(accessToken, null, request.getIndex(), "question");
//            //fcm
//            Member partner = memberService.getPartnerByAccessToken(accessToken);
//            String fcmDetail = firebaseCloudMessageService.ChattingMessageFcmJsonObject(
//                    "textChatting",
//                    chattingMessage.getIsRead(),
//                    chattingMessage.getMessage(),
//                    chattingMessage.getNumber(),
//                    chattingMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));()
//            );
//            firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());
//            //response
//            JSONObject coupleQuestionObject = new JSONObject();
//            CoupleQuestion coupleQuestion = coupleQuestionService.findByIndex(accessToken, Math.toIntExact(request.getIndex()));
//            coupleQuestionObject.put("index", coupleQuestion.getNumber());
//            coupleQuestionObject.put("questionTitle", coupleQuestion.getQuestion().getTitle());
//            coupleQuestionObject.put("selfAnswer", coupleQuestion.getSelfAnswer());
//            coupleQuestionObject.put("partnerAnswer", coupleQuestion.getPartnerAnswer());
//
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("coupleQuestion", coupleQuestionObject);
//            jsonObject.put("index", chattingMessage.getNumber());
//            jsonObject.put("isRead", chattingMessage.getIsRead());
//            jsonObject.put("createdAt", chattingMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));());
//
//            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
//        } catch (NullPointerException e) {
//            throw e;
////            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
//        }
//    }

    @GetMapping("/api/v1/questioncharlogic")
    public void quesitonchar() {
        List<Question> all = questionService.findAll();
//        Question question = questionService.findQuestionByTitle("난 이게 가장 좋더라 ! 내가 가장 좋아하는 스킨십은 ?");


        for (Question question : all) {
            String title = question.getTitle();
            List<QuestionChar> chars = question.getQuestionChars();
            for (int i = 0; i < title.length(); i++) {

                char ch = title.charAt(i);
                if (ch == ' ') {
                    ch = '_';
                }
                QuestionChar questionChar = QuestionChar.builder()
                        .number((long) i)
                        .ch(ch)
                        .type("header")
                        .bold(false)
                        .color("black")
                        .question(question)
                        .build();

                questionCharService.join(questionChar);
                chars.add(questionChar);
            }
            question.setQuestionChars(chars);
            questionService.join(question);

        }
    }

    @GetMapping("/api/v1/couple-question/{id}")
    public JSendResponse getCoupleQuestionById(@RequestHeader("Authorization") String authorizationHeader,
                                               @PathVariable("id") int id) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            CoupleQuestion question = coupleQuestionService.getCoupleQuestion(accessToken, id);
            Member partner = memberService.getPartnerByAccessToken(accessToken);
            log.info("데이터 가져오기성공");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", question.getQuestion().getNumber());
            jsonObject.put("pageNo", question.getQuestion().getNumber() / 4);
            jsonObject.put("title", question.getQuestion().getTitle());
            jsonObject.put("header", question.getQuestion().getHeader());
            jsonObject.put("body", question.getQuestion().getBody());
            jsonObject.put("highlight", questionCharService.getHighlight(question.getQuestion().getId()));
            if (memberService.getMyPosition(accessToken).equals("self")) {
                jsonObject.put("myAnswer", question.getSelfAnswer());
                jsonObject.put("partnerAnswer", question.getPartnerAnswer());
                jsonObject.put("isMyAnswer", (question.getSelfAnswer() != null));
                jsonObject.put("isPartnerAnswer", (question.getPartnerAnswer() != null));
            } else {
                jsonObject.put("myAnswer", question.getPartnerAnswer());
                jsonObject.put("partnerAnswer", question.getSelfAnswer());
                jsonObject.put("isMyAnswer", (question.getPartnerAnswer() != null));
                jsonObject.put("isPartnerAnswer", (question.getSelfAnswer() != null));
            }
            jsonObject.put("createAt", question.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.getMessage());
        }
    }

    @PutMapping("/api/v1/couple-question")
    public JSendResponse answerQuestion(@RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody AnswerQuestionRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member member = memberService.getCurrentMember();
            Couple couple = coupleService.getCouple(member);
            CoupleQuestion coupleQuestion = coupleQuestionService.updateSelfAnswer(accessToken, request.getIndex(), request.getMyAnswer());
            String fcmDetail = firebaseCloudMessageService.getFcmAnswerQuestion(
                    "answerQuestion",
                    request.index,
                    "연인이 질문에 답변했어요",
                    "지금 바로 확인해보세요"
            );
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
            firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.getMessage());
        }
    }

    @GetMapping("/api/v1/couple-question/today")
    public JSendResponse getRecentQuestion(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            CoupleQuestion question = coupleQuestionService.getRecent(accessToken);
            log.info("데이터 가져오기성공");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", question.getQuestion().getNumber());
            jsonObject.put("pageNo", question.getQuestion().getNumber() / 4);
            jsonObject.put("title", question.getQuestion().getTitle());
            jsonObject.put("header", question.getQuestion().getHeader());
            jsonObject.put("body", question.getQuestion().getBody());
            jsonObject.put("highlight", questionCharService.getHighlight(question.getQuestion().getId()));
            if (memberService.getMyPosition(accessToken).equals("self")) {
                jsonObject.put("myAnswer", question.getSelfAnswer());
                jsonObject.put("partnerAnswer", question.getPartnerAnswer());
                jsonObject.put("isMyAnswer", (question.getSelfAnswer() != null));
                jsonObject.put("isPartnerAnswer", (question.getPartnerAnswer() != null));
            } else {
                jsonObject.put("myAnswer", question.getPartnerAnswer());
                jsonObject.put("partnerAnswer", question.getSelfAnswer());
                jsonObject.put("isMyAnswer", (question.getPartnerAnswer() != null));
                jsonObject.put("isPartnerAnswer", (question.getSelfAnswer() != null));
            }
            jsonObject.put("createAt", question.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));


            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            log.info("today question 에러발생");
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.getMessage());
        }
    }

    @PostMapping("/api/v1/couple-question/chase-up")
    public JSendResponse pressForAnswer(@RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody PressForAnswerRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member member = memberService.getCurrentMember();
            Couple couple = coupleService.getCouple(member);


            ChattingMessage chattingMessage = chattingRoomService.addPressForAnswerChattingMessage();
            //response
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", chattingMessage.getNumber());
            jsonObject.put("isRead", chattingMessage.getIsRead());
            jsonObject.put("createAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.getMessage());
        }
    }

    @PostMapping("/api/v1/couple-questions")
    public JSendResponse getLatestChatPageNo(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody CoupleQuestionPageNoRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            List<CoupleQuestion> list = coupleQuestionService.getRecentList(accessToken, request.getPageNo());

            JSONArray jsonArray = new JSONArray();
            for (int i = list.size() - 1; i >= 0; i--) {
                CoupleQuestion question = list.get(i);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("index", question.getQuestion().getNumber());
                jsonObject.put("pageNo", question.getQuestion().getNumber() / 4);
                jsonObject.put("title", question.getQuestion().getTitle());
                jsonObject.put("header", question.getQuestion().getHeader());
                jsonObject.put("body", question.getQuestion().getBody());
                jsonObject.put("highlight", questionCharService.getHighlight(question.getQuestion().getId()));
                if (memberService.getMyPosition(accessToken).equals("self")) {
                    jsonObject.put("myAnswer", question.getSelfAnswer());
                    jsonObject.put("partnerAnswer", question.getPartnerAnswer());
                    jsonObject.put("isMyAnswer", (question.getSelfAnswer() != null));
                    jsonObject.put("isPartnerAnswer", (question.getPartnerAnswer() != null));
                } else {
                    jsonObject.put("myAnswer", question.getPartnerAnswer());
                    jsonObject.put("partnerAnswer", question.getSelfAnswer());
                    jsonObject.put("isMyAnswer", (question.getPartnerAnswer() != null));
                    jsonObject.put("isPartnerAnswer", (question.getSelfAnswer() != null));
                }
                jsonObject.put("createAt", question.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                jsonArray.add(jsonObject);

            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("questions", jsonArray);

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }

    }

    /***
     * 왜만들었더라..
     * @param authorizationHeader
     * @param request
     * @return
     */
    @PostMapping("/api/v1/couple-questions/DESC")
    public JSendResponse getCoupleQuestionsDESC(@RequestHeader("Authorization") String authorizationHeader,
                                                @RequestBody CoupleQuestionPageNoRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            List<CoupleQuestion> list = coupleQuestionService.getRecentListDESC(accessToken, request.getPageNo());

            JSONArray jsonArray = new JSONArray();
            for (CoupleQuestion question : list) {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("index", question.getQuestion().getNumber());
                jsonObject.put("pageNo", question.getQuestion().getNumber() / 4);
                jsonObject.put("title", question.getQuestion().getTitle());
                jsonObject.put("header", question.getQuestion().getHeader());
                jsonObject.put("body", question.getQuestion().getBody());
                jsonObject.put("highlight", questionCharService.getHighlight(question.getQuestion().getId()));
                if (memberService.getMyPosition(accessToken).equals("self")) {
                    jsonObject.put("myAnswer", question.getSelfAnswer());
                    jsonObject.put("partnerAnswer", question.getPartnerAnswer());
                    jsonObject.put("isMyAnswer", (question.getSelfAnswer() != null));
                    jsonObject.put("isPartnerAnswer", (question.getPartnerAnswer() != null));
                } else {
                    jsonObject.put("myAnswer", question.getPartnerAnswer());
                    jsonObject.put("partnerAnswer", question.getSelfAnswer());
                    jsonObject.put("isMyAnswer", (question.getPartnerAnswer() != null));
                    jsonObject.put("isPartnerAnswer", (question.getSelfAnswer() != null));
                }
                jsonObject.put("createAt", question.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                jsonArray.add(jsonObject);

            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("questions", jsonArray);

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }

    }

    @Data
    static class CoupleQuestionPageNoRequest {
        private int pageNo;
    }

    @Data
    static class AnswerQuestionRequest {
        private int index;
        private String myAnswer;
    }

    @Data
    static class PressForAnswerRequest {
        private int index;
    }
}