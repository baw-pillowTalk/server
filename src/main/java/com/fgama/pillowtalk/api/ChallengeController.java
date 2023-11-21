package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.constant.HttpResponse;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.CoupleChallenge;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.domain.chattingMessage.ChattingMessage;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChallengeController {
    private final ChallengeService challengeService;
    private final MemberService memberService;
    private final CoupleService coupleService;
    private final ChallengeJsonService challengeJsonService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;
    private final ChattingRoomService chattingRoomService;

    @GetMapping("/api/v1/challenge/count")
    public JSendResponse getCount(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            List<Integer> counts = challengeService.getCount(accessToken);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("totalCount", counts.get(0));
            jsonObject.put("ongoingCount", counts.get(1));
            jsonObject.put("completedCount", counts.get(2));


            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/api/v1/challenge/{id}")
    public JSendResponse getChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                      @PathVariable("id") int id) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            CoupleChallenge coupleChallenge = challengeService.getChallenge(accessToken, id);

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, challengeJsonService.getChallengeData(coupleChallenge));
        } catch (Exception e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }
    }

    @GetMapping("/api/v1/challenge/in-progress/last/page-no")
    public JSendResponse getLatestOngoingChallengePageNo(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            int latestChallengePageNo = challengeService.getLatestChallengePageNoInProgress(accessToken);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pageNo", latestChallengePageNo);

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }

    }

    @PostMapping("/api/v1/challenges/in-progress")
    public JSendResponse getInProgressChallengeList(@RequestHeader("Authorization") String authorizationHeader,
                                                    @RequestBody ChallengeListRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());

            List<CoupleChallenge> list = challengeService.getInProgressChallengeList(accessToken, request.getPageNo());

            JSONArray jsonArray = new JSONArray();
            for (int i = list.size() - 1; i >= 0; i--) {
                CoupleChallenge coupleChallenge = list.get(i);
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("index", coupleChallenge.getNumber());
                jsonMessage.put("pageNo", coupleChallenge.getNumber() / 4);
//                jsonMessage.put("category", challenge.getCategory());
                jsonMessage.put("title", coupleChallenge.getTitle());
                jsonMessage.put("deadline", coupleChallenge.getTargetDate());
                jsonMessage.put("content", coupleChallenge.getBody());
                jsonMessage.put("creator", coupleChallenge.getCreator());
                jsonMessage.put("isCompleted", coupleChallenge.getDone());
                jsonArray.add(jsonMessage);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("challengeList", jsonArray);


            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }
    }

    @GetMapping("/api/v1/challenge/done/last/page-no")
    public JSendResponse getLatestDoneChallengePageNo(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            int latestChallengePageNo = challengeService.getLatestChallengePageNoDone(accessToken);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pageNo", latestChallengePageNo);

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }

    }

    @PostMapping("/api/v1/challenges/done")
    public JSendResponse getDoneChallengeList(@RequestHeader("Authorization") String authorizationHeader,
                                              @RequestBody ChallengeListRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());

            List<CoupleChallenge> list = challengeService.getDoneChallengeList(accessToken, request.getPageNo());

            JSONArray jsonArray = new JSONArray();

            for (int i = list.size() - 1; i >= 0; i--) {
                CoupleChallenge coupleChallenge = list.get(i);
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("index", coupleChallenge.getNumber());
                jsonMessage.put("pageNo", coupleChallenge.getNumber() / 4);
//                jsonMessage.put("category", challenge.getCategory());
                jsonMessage.put("title", coupleChallenge.getTitle());
                jsonMessage.put("deadline", coupleChallenge.getTargetDate());
                jsonMessage.put("content", coupleChallenge.getBody());
                jsonMessage.put("creator", coupleChallenge.getCreator());
                jsonMessage.put("isCompleted", coupleChallenge.getDone());
                jsonArray.add(jsonMessage);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("challengeList", jsonArray);


            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }
    }

    @PostMapping("/api/v1/challenge")
    public JSendResponse addChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                      @RequestBody AddChallengeRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member member = memberService.getCurrentMember();
            Long coupleId = member.getCoupleId();
            Couple couple = coupleService.getCouple(member);
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
            CoupleChallenge coupleChallengeServiceByIndex = challengeService.addChallenge(accessToken, request.getTitle(), request.getContent(), request.getDeadline());
            ChattingMessage chattingMessage = chattingRoomService.addChallengeChattingMessage(coupleChallengeServiceByIndex.getNumber());
            //fcm

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
            jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);
            jsonObject.put("createAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));




            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }
    }

    @PutMapping("/api/v1/challenge")
    public JSendResponse updateChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestBody UpdateChallengeRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member member = memberService.getCurrentMember();
            Couple couple = coupleService.getCouple(member);
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();

            CoupleChallenge coupleChallenge = challengeService.updateChallenge(accessToken, request.getIndex(), request.getTitle(), request.getContent(), request.getDeadline());
            String fcmDetail = firebaseCloudMessageService.addChallengeFcmJsonObject(
                    "modifyChallenge",
                    coupleChallenge.getNumber()

            );
            firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null);
        } catch (Exception e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }
    }

    @Transactional
    @DeleteMapping("/api/v1/challenge")
    public JSendResponse deleteChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestBody DeleteChallengeRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            challengeService.deleteChallenge(accessToken, request.getIndex());
            Member member = memberService.getCurrentMember();

            Couple couple = coupleService.getCouple(member);
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();

            String fcmDetail = firebaseCloudMessageService.addChallengeFcmJsonObject(
                    "deleteChallenge",
                    request.getIndex()

            );
            firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

            challengeService.sortNumber(accessToken);
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null);
        } catch (Exception e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }
    }

    @PutMapping("/api/v1/challenge/complete")
    public JSendResponse doneChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                       @RequestBody CompleteChallengeRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            challengeService.doneChallenge(request.getIndex());
            Member member = memberService.getCurrentMember();
            Couple couple = coupleService.getCouple(member);
            ChattingMessage chattingMessage = chattingRoomService.addCompleteChallengeChattingMessage(request.getIndex());

            CoupleChallenge coupleChallenge = challengeService.findByIndex(accessToken, request.getIndex());
            //response
            JSONObject coupleChallengeObject = new JSONObject();

            coupleChallengeObject.put("index", coupleChallenge.getNumber());
//            coupleChallengeObject.put("category",challengeServiceByIndex.getCategory());
            coupleChallengeObject.put("challengeTitle", coupleChallenge.getTitle());
            coupleChallengeObject.put("challengeBody", coupleChallenge.getBody());
            coupleChallengeObject.put("deadline", coupleChallenge.getTargetDate());
            coupleChallengeObject.put("creator", memberService.getCurrentMember().getNickname());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("coupleChallenge", coupleChallengeObject);
            jsonObject.put("index", chattingMessage.getNumber());
            jsonObject.put("pageIndex", chattingMessage.getNumber() / 4);
            jsonObject.put("isRead", chattingMessage.getIsRead());
            jsonObject.put("createAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }
    }

    /***
     * 12시마다 챌린지 기간 지난거 완료로 보내는 로직
     */
    @Transactional
    @Scheduled(cron = "0  0  0  *  *  *")
    public void arrangeChallenge() {
        log.info("챌린지 업데이트");
        List<CoupleChallenge> all = challengeService.findAll();
        for (CoupleChallenge coupleChallenge : all) {
            int compareTo = coupleChallenge.getTargetDate().compareTo(LocalDateTime.now());

            if (compareTo < 0) {
                coupleChallenge.setDone(true);
                challengeService.join(coupleChallenge);
            }
        }
    }

    @Data
    static class AddChallengeRequest {

        //        private String category;
        private String title;
        private String deadline;
        private String content;
    }

    @Data
    static class ChallengeListRequest {
        private int pageNo;
    }

    @Data
    static class UpdateChallengeRequest {

        private int index;
        //        private String category;
        private String title;
        private String deadline;
        private String content;
    }

    @Data
    static class DeleteChallengeRequest {
        private int index;

    }

    @Data
    static class CompleteChallengeRequest {
        private int index;

    }

    @Data
    static class ChallengeRequest {
        private Long id;

    }

}