package com.fgama.pillowtalk.fcm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fgama.pillowtalk.domain.CoupleChallenge;
import com.fgama.pillowtalk.domain.CoupleQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
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

@Component
@Slf4j
@RequiredArgsConstructor
public class FirebaseCloudMessageService {


    private final ObjectMapper objectMapper;
    @Value("${fcm.url}")
    private String API_URL;
    @Value("${fcm.key}")
    private String FCM_KEY;

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

    public String questionMessageFcmJsonObject(String type, Boolean isRead, CoupleQuestion coupleQuestion, Long index, LocalDateTime createAt) {
        JSONObject coupleQuestionObject = new JSONObject();
        coupleQuestionObject.put("index", coupleQuestion.getNumber());
        coupleQuestionObject.put("questionTitle", coupleQuestion.getQuestion().getTitle());
        coupleQuestionObject.put("selfAnswer", coupleQuestion.getSelfAnswer());
        coupleQuestionObject.put("partnerAnswer", coupleQuestion.getPartnerAnswer());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("coupleQuestion", coupleQuestionObject);
        jsonObject.put("index", index);
        jsonObject.put("createAt", createAt);
        jsonObject.put("isRead", isRead);

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

    public String addChallengeFcmJsonObject(String type, int index) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("index", index);
        return jsonObject.toJSONString();
    }

    public String voiceMessageFcmJsonObject(String type, Boolean isRead, String url, Long index, Long pageIndex, LocalDateTime createdAt) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("url", url);
        jsonObject.put("index", index);
        jsonObject.put("pageIndex", pageIndex);
        jsonObject.put("createAt", createdAt.toString());
        jsonObject.put("isRead", isRead);

        return jsonObject.toJSONString();
    }

    public String imageMessageFcmJsonObject(String type, Boolean isRead, String url, Long index, Long pageIndex, LocalDateTime createdAt) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("url", url);
        jsonObject.put("index", index);
        jsonObject.put("pageIndex", pageIndex);
        jsonObject.put("createAt", createdAt.toString());
        jsonObject.put("isRead", isRead);

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