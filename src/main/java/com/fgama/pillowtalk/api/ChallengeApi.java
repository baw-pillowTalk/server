package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.config.Constants;
import com.fgama.pillowtalk.domain.Challenge;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.service.ChallengeJsonService;
import com.fgama.pillowtalk.service.ChallengeService;
import com.fgama.pillowtalk.service.CoupleService;
import com.fgama.pillowtalk.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChallengeApi {
    private final ChallengeService challengeService;
    private final MemberService memberService;
    private final CoupleService coupleService;
    private final ChallengeJsonService challengeJsonService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @GetMapping("/api/v1/challenge/count")
    public JSendResponse getCount(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            List<Integer> counts = challengeService.getCount(accessToken);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("totalCount", counts.get(0));
            jsonObject.put("ongoingCount", counts.get(1));
            jsonObject.put("completedCount", counts.get(2));


            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/api/v1/challenge/{id}")
    public JSendResponse getChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                      @PathVariable("id") long id) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Challenge challenge = challengeService.getChallenge(accessToken, id);

            return new JSendResponse(Constants.HTTP_SUCCESS, null, challengeJsonService.getChallengeData(challenge));
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    @GetMapping("/api/v1/challenge/in-progress/last/page-no")
    public JSendResponse getLatestOngoingChallengePageNo(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            int latestChallengePageNo = challengeService.getLatestChallengePageNoInProgress(accessToken);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pageNo", latestChallengePageNo);

            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }

    }

    @PostMapping("/api/v1/challenges/in-progress")
    public JSendResponse getInProgressChallengeList(@RequestHeader("Authorization") String authorizationHeader,
                                                    @RequestBody ChallengeListRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());

            List<Challenge> list = challengeService.getInProgressChallengeList(accessToken, request.getPageNo());

            JSONArray jsonArray = new JSONArray();
            for (int i = list.size() - 1; i >= 0; i--) {
                Challenge challenge = list.get(i);
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("index", challenge.getNumber());
                jsonMessage.put("pageNo", challenge.getNumber() / 4);
//                jsonMessage.put("category", challenge.getCategory());
                jsonMessage.put("title", challenge.getTitle());
                jsonMessage.put("deadline", challenge.getTargetDate());
                jsonMessage.put("content", challenge.getBody());
                jsonMessage.put("creator", challenge.getCreator());
                jsonMessage.put("isCompleted", challenge.getDone());
                jsonArray.add(jsonMessage);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("challengeList", jsonArray);


            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    @GetMapping("/api/v1/challenge/done/last/page-no")
    public JSendResponse getLatestDoneChallengePageNo(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            int latestChallengePageNo = challengeService.getLatestChallengePageNoDone(accessToken);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pageNo", latestChallengePageNo);

            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }

    }

    @PostMapping("/api/v1/challenges/done")
    public JSendResponse getDoneChallengeList(@RequestHeader("Authorization") String authorizationHeader,
                                              @RequestBody ChallengeListRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());

            List<Challenge> list = challengeService.getDoneChallengeList(accessToken, request.getPageNo());

            JSONArray jsonArray = new JSONArray();

            for (int i = list.size() - 1; i >= 0; i--) {
                Challenge challenge = list.get(i);
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("index", challenge.getNumber());
                jsonMessage.put("pageNo", challenge.getNumber() / 4);
//                jsonMessage.put("category", challenge.getCategory());
                jsonMessage.put("title", challenge.getTitle());
                jsonMessage.put("deadline", challenge.getTargetDate());
                jsonMessage.put("content", challenge.getBody());
                jsonMessage.put("creator", challenge.getCreator());
                jsonMessage.put("isCompleted", challenge.getDone());
                jsonArray.add(jsonMessage);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("challengeList", jsonArray);


            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    @PostMapping("/api/v1/challenge")
    public JSendResponse addChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                      @RequestBody AddChallengeRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member member = memberService.findMemberByAccessToken(accessToken);

            Long coupleId = member.getCoupleId();
            Couple couple = coupleService.findCoupleById(coupleId);
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
            Challenge challenge = challengeService.addChallenge(accessToken, request.getTitle(), request.getContent(), request.getDeadline());
            String fcmDetail = firebaseCloudMessageService.addChallengeFcmJsonObject(
                    "addChallenge",
                    challenge.getNumber()

            );
            firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", challenge.getNumber());

            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    @PutMapping("/api/v1/challenge")
    public JSendResponse updateChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestBody UpdateChallengeRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member member = memberService.findMemberByAccessToken(accessToken);

            Long coupleId = member.getCoupleId();
            Couple couple = coupleService.findCoupleById(coupleId);
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();

            Challenge challenge = challengeService.updateChallenge(accessToken, request.getIndex(), request.getTitle(), request.getContent(), request.getDeadline());
            String fcmDetail = firebaseCloudMessageService.addChallengeFcmJsonObject(
                    "modifyChallenge",
                    challenge.getNumber()

            );
            firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    @Transactional
    @DeleteMapping("/api/v1/challenge")
    public JSendResponse deleteChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestBody DeleteChallengeRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            challengeService.deleteChallenge(accessToken, request.getIndex());
            Member member = memberService.findMemberByAccessToken(accessToken);

            Long coupleId = member.getCoupleId();
            Couple couple = coupleService.findCoupleById(coupleId);
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();

            String fcmDetail = firebaseCloudMessageService.addChallengeFcmJsonObject(
                    "deleteChallenge",
                    request.getIndex()

            );
            firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

            challengeService.sortNumber(accessToken);
            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    @PutMapping("/api/v1/challenge/complete")
    public JSendResponse doneChallenge(@RequestHeader("Authorization") String authorizationHeader,
                                       @RequestBody CompleteChallengeRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            challengeService.doneChallenge(accessToken, request.getIndex());
            Member member = memberService.findMemberByAccessToken(accessToken);

            Long coupleId = member.getCoupleId();
            Couple couple = coupleService.findCoupleById(coupleId);
            Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();

            String fcmDetail = firebaseCloudMessageService.addChallengeFcmJsonObject(
                    "completeChallenge",
                    request.getIndex()

            );
            firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());

            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    /***
     * 12시마다 챌린지 기간 지난거 완료로 보내는 로직
     */
    @Transactional
    @Scheduled(cron = "0  0  0  *  *  *")
    public void arrangeChallenge() {
        log.info("챌린지 업데이트");
        List<Challenge> all = challengeService.findAll();
        for (Challenge challenge : all) {
            int compareTo = challenge.getTargetDate().compareTo(LocalDateTime.now());

            if (compareTo < 0) {
                challenge.setDone(true);
                challengeService.join(challenge);
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

        private Long index;
        //        private String category;
        private String title;
        private String deadline;
        private String content;
    }

    @Data
    static class DeleteChallengeRequest {
        private Long index;

    }

    @Data
    static class CompleteChallengeRequest {
        private Long index;

    }

    @Data
    static class ChallengeRequest {
        private Long id;

    }

}