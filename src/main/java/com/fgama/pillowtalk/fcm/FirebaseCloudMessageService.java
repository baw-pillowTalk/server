package com.fgama.pillowtalk.fcm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgama.pillowtalk.domain.CoupleChallenge;
import com.fgama.pillowtalk.domain.CoupleQuestion;
import com.fgama.pillowtalk.domain.chattingMessage.*;
import com.fgama.pillowtalk.repository.CoupleQuestionRepository;
import io.netty.util.Signal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import okhttp3.Challenge;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class FirebaseCloudMessageService {


    private final ObjectMapper objectMapper;
    @Value("${fcm.url}")
    private String API_URL;
    @Value("${fcm.key}")
    private String FCM_KEY;

    private final CoupleQuestionRepository coupleQuestionRepository;

    public String sendFcmMessage(String message, String fcmToken) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
//        try {
//            HttpUriRequest http_post = RequestBuilder.post()
//                    .setUri(new URI(API_URL))
//                    .addHeader("Authorization", FCM_KEY)
//                    .addParameter("content_available","true")
//                    .addParameter("priority","normal")
//                    .addParameter("data", message)
//                    .addParameter("to", fcmToken)
//                    .build();
//
//            CloseableHttpResponse response = httpclient.execute(http_post);
//            System.out.println(http_post);
//            try {
//                System.out.println(EntityUtils.toString(response.getEntity()));
//                return "ok";
//            } finally {
//                response.close();
//            }
        try {
            HttpPost http_post = new HttpPost(API_URL);
            http_post.addHeader("Authorization", FCM_KEY);
            http_post.addHeader("Content-Type", "application/json"); // JSON 형태의 데이터를 전송한다는 설정

            String jsonData = "{ \"content_available\": true, \"priority\": \"normal\", \"data\": " + message + ", \"to\": \"" + fcmToken + "\" }";
//            String jsonData = "{ \"content_available\": true, \"priority\": \"normal\", \"notification\": " + message + ", \"to\": \"" + fcmToken + "\" }";
            StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
            http_post.setEntity(entity);
            log.info("FCM post Entity" + entity);
            CloseableHttpResponse response = httpclient.execute(http_post);
            log.info("FCM post response" + response);
            System.out.println(http_post);
            try {
                System.out.println(EntityUtils.toString(response.getEntity()));
                return "ok";
            } finally {
                response.close();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getFcmJsonObject(String title, String type, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", title);
        jsonObject.put("type", type);
        jsonObject.put("message", message);
        return jsonObject.toJSONString();
    }

    public String getFcmChattingStatus(String type, Boolean isInChat) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("isInChat", isInChat);
        return jsonObject.toJSONString();
    }

    public String getFcmAnswerQuestion(String type, int index, String title, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("index", index);
        jsonObject.put("title", title);
        jsonObject.put("message", message);
        return jsonObject.toJSONString();
    }

    public String getFcmPressAnswer(String type, int index) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("index", index);
        return jsonObject.toJSONString();
    }

    public String getQuestionFcmJsonObject(String title, String type, String message, String detail) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", title);
        jsonObject.put("type", type);
        jsonObject.put("message", message);
        jsonObject.put("detail", detail);
        return jsonObject.toJSONString();
    }

    public String getCoupleQuestionFcmJsonObject(String title, String type, String message, int index) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", title);
        jsonObject.put("type", type);
        jsonObject.put("message", message);
        jsonObject.put("index", index);
        return jsonObject.toJSONString();
    }

    public String getFcmChattingMessageJsonObject(
            String title, String title_detail, String type,
            String message, String message_detail, String createAt,
            String from_accessToken) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", title);
        jsonObject.put("title_detail", title_detail);
        jsonObject.put("type", type);
        jsonObject.put("message", message);
        jsonObject.put("message_detail", message_detail);
        jsonObject.put("createAt", createAt);
        jsonObject.put("from_accessToken", from_accessToken);

        return jsonObject.toJSONString();
    }

    public String chattingMessageFcmJsonObject(String type, Boolean isRead, String message, Long index, Long pageIndex, LocalDateTime createdAt) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("message", message);
        jsonObject.put("index", index);
        jsonObject.put("pageIndex", pageIndex);
        jsonObject.put("createAt", createdAt.toString());
        jsonObject.put("isRead", isRead);

        return jsonObject.toJSONString();
    }

    public String textMessageFcmJsonObject(TextChattingMessage chattingMessage) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("message", chattingMessage.getMessage());
        jsonObject.put("type", "textChatting");
        jsonObject.put("index", chattingMessage.getNumber());
        jsonObject.put("isRead", chattingMessage.getIsRead());
        jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

        return jsonObject.toJSONString();
    }

    public String questionMessageFcmJsonObject(QuestionChattingMessage chattingMessage, CoupleQuestion coupleQuestion) {
        if (coupleQuestion.getSelfAnswer() == null || coupleQuestion.getPartnerAnswer() == null) {
            JSONObject questionObject = new JSONObject();
            questionObject.put("index", coupleQuestion.getNumber());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("coupleQuestion", questionObject);
            jsonObject.put("type", "answerChatting");
            jsonObject.put("index", chattingMessage.getNumber());
            jsonObject.put("isRead", chattingMessage.getIsRead());
            jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

            return jsonObject.toJSONString();
        } else {
            JSONObject questionObject = new JSONObject();
            questionObject.put("index", coupleQuestion.getNumber());
            questionObject.put("questionTitle", coupleQuestion.getQuestion().getTitle());
            questionObject.put("selfAnswer", coupleQuestion.getSelfAnswer());
            questionObject.put("partnerAnswer", coupleQuestion.getPartnerAnswer());
            questionObject.put("createAt", coupleQuestion.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("coupleQuestion", questionObject);
            jsonObject.put("type", "questionChatting");
            jsonObject.put("index", chattingMessage.getNumber());
            jsonObject.put("isRead", chattingMessage.getIsRead());
            jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

            return jsonObject.toJSONString();
        }
    }

    public String imageMessageFcmJsonObject(ImageChattingMessage chattingMessage, String url) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", url);

        jsonObject.put("type", "imageChatting");
        jsonObject.put("index", chattingMessage.getNumber());
        jsonObject.put("isRead", chattingMessage.getIsRead());
        jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

        return jsonObject.toJSONString();
    }

    public String voiceMessageFcmJsonObject(VoiceChattingMessage chattingMessage, String url) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "voiceChatting");
        jsonObject.put("url", url);
        jsonObject.put("index", chattingMessage.getNumber());
        jsonObject.put("isRead", chattingMessage.getIsRead());
        jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

        return jsonObject.toJSONString();
    }

    public String challengeMessageFcmJsonObject(ChallengeChattingMessage chattingMessage, CoupleChallenge coupleChallenge) {
        JSONObject questionObject = new JSONObject();
        questionObject.put("index", coupleChallenge.getNumber());
        questionObject.put("challengeTitle", coupleChallenge.getTitle());
        questionObject.put("challengeBody", coupleChallenge.getBody());
        questionObject.put("deadline", coupleChallenge.getTargetDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        questionObject.put("creator", coupleChallenge.getCreator());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("coupleChallenge", questionObject);
        jsonObject.put("type", "challengeChatting");
        jsonObject.put("index", chattingMessage.getNumber());
        jsonObject.put("isRead", chattingMessage.getIsRead());
        jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

        return jsonObject.toJSONString();
    }

    public String addChallengeMessageFcmJsonObject(ChallengeChattingMessage chattingMessage, CoupleChallenge coupleChallenge) {
        JSONObject questionObject = new JSONObject();
        questionObject.put("index", coupleChallenge.getNumber());
        questionObject.put("challengeTitle", coupleChallenge.getTitle());
        questionObject.put("challengeBody", coupleChallenge.getBody());
        questionObject.put("deadline", coupleChallenge.getTargetDate());
        questionObject.put("creator", coupleChallenge.getCreator());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("coupleChallenge", questionObject);
        jsonObject.put("type", "addChallengeChatting");
        jsonObject.put("index", chattingMessage.getNumber());
        jsonObject.put("isRead", chattingMessage.getIsRead());
        jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

        return jsonObject.toJSONString();
    }

    public String completeChallengeMessageFcmJsonObject(CompleteChallengeChattingMessage chattingMessage, CoupleChallenge coupleChallenge) {
        JSONObject completeChallengeObject = new JSONObject();
        completeChallengeObject.put("index", coupleChallenge.getNumber());
        completeChallengeObject.put("challengeTitle", coupleChallenge.getTitle());
        completeChallengeObject.put("challengeBody", coupleChallenge.getBody());
        completeChallengeObject.put("deadline", coupleChallenge.getTargetDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        completeChallengeObject.put("creator", coupleChallenge.getCreator());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("coupleChallenge", completeChallengeObject);
        jsonObject.put("type", "completeChallengeChatting");
        jsonObject.put("index", chattingMessage.getNumber());
        jsonObject.put("isRead", chattingMessage.getIsRead());
        jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

        return jsonObject.toJSONString();
    }

    public String signalMessageFcmJsonObject(SignalChattingMessage chattingMessage) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("signal", chattingMessage.getSignalPercent());
        jsonObject.put("type", "signalChatting");
        jsonObject.put("index", chattingMessage.getNumber());
        jsonObject.put("isRead", chattingMessage.getIsRead());
        jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

        return jsonObject.toJSONString();
    }

    public String resetPartnerPasswordMessageFcmJsonObject(ResetPasswordChattingMessage chattingMessage) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("type", "resetPartnerPasswordChatting");
        jsonObject.put("index", chattingMessage.getNumber());
        jsonObject.put("isRead", chattingMessage.getIsRead());
        jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

        return jsonObject.toJSONString();
    }

    public String pressForAnswerMessageFcmJsonObject(PressForAnswerChattingMessage chattingMessage, CoupleQuestion coupleQuestion) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("coupleQuestion", coupleQuestion.getNumber());
        jsonObject.put("type", "pressForAnswerChatting");
        jsonObject.put("index", chattingMessage.getNumber());
        jsonObject.put("isRead", chattingMessage.getIsRead());
        jsonObject.put("createAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);

        return jsonObject.toJSONString();
    }

    public String addChallengeFcmJsonObject(String type, int index) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("index", index);
        return jsonObject.toJSONString();
    }


    public String emojiMessageFcmJsonObject(String type, Boolean isRead, String emoji, Long index, LocalDateTime createAt) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("emoji", emoji);
        jsonObject.put("index", index);
        jsonObject.put("createAt", createAt);
        jsonObject.put("isRead", isRead);

        return jsonObject.toJSONString();
    }

    public String shareChallengeFcmJsonObject(String type, Boolean isRead, CoupleChallenge coupleChallenge, Long index, LocalDateTime createAt) {


        JSONObject challengeObject = new JSONObject();
        challengeObject.put("index", coupleChallenge.getNumber());
        challengeObject.put("challengeTitle", coupleChallenge.getTitle());
        challengeObject.put("challengeBody", coupleChallenge.getBody());
        challengeObject.put("deadline", coupleChallenge.getTargetDate());
        challengeObject.put("creator", coupleChallenge.getCreator());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("coupleChallenge", challengeObject);
        jsonObject.put("index", index);
        jsonObject.put("createAt", createAt);
        jsonObject.put("isRead", isRead);

        return jsonObject.toJSONString();
    }

    public String makeMessage(String title, String type, String message) throws
            JsonProcessingException {
        return objectMapper.writeValueAsString(FcmMessage.builder()
                .title(title)
                .message(message)
                .build());
    }

    @Data
    @AllArgsConstructor
    static class EmotionResponse {
        private String status;
    }
}