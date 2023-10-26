package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.config.Constants;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.domain.MemberConfig;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.service.ChallengeService;
import com.fgama.pillowtalk.service.CoupleQuestionService;
import com.fgama.pillowtalk.service.CoupleService;
import com.fgama.pillowtalk.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MemberApiV1 {
    private final MemberService memberService;
    private final CoupleService coupleService;
    private final ChallengeService challengeService;
    private final CoupleQuestionService coupleQuestionService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @GetMapping("/api/v1/member/invite-code")
    public JSendResponse getInviteCode(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member member = memberService.findMemberByAccessTokenThrow(accessToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("inviteCode", member.getInviteCode());
            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }


    /***
     * 채팅방 상태변경
     * 유저가 채팅을 읽고있는 상태 or 읽지 않는 상태 변경경     * @param authorizationHeader
     * @param request
     * @return
     */
    @PutMapping("/api/v1/member/chatting-room-status")
    public JSendResponse changeChatRoomStatus(@RequestHeader("Authorization") String authorizationHeader,
                                              @RequestBody ChangeStatusRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.changeChatRoomStatus(accessToken, request.getIsInChat());

            Member partner = memberService.getPartnerByAccessToken(accessToken);
            String fcmDetail = firebaseCloudMessageService.getFcmChattingStatus(
                    "chatRoomStatusChange",
                    request.getIsInChat()
            );
            firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());
            log.info("fcm status");
            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    /***
     * 탈퇴 직전 유저가 가진 데이터
     * @param authorizationHeader
     * @return
     */
    @GetMapping("api/v1/member/service-data")
    public JSendResponse getServiceData(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            List<Integer> count = challengeService.getCount(accessToken);
            Integer challengeTotalCount = count.get(0);

            //questionTotalCount
            int questionTotalCount = coupleQuestionService.getTotalCount(accessToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("challengeTotalCount", challengeTotalCount);
            jsonObject.put("questionTotalCount", questionTotalCount);

            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @PutMapping("api/v1/member/fcm-token")
    public JSendResponse updateFcmToken(@RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody UpdateFcmTokenRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.setFcmToken(accessToken, request.getFcmToken());
            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @PutMapping("api/v1/member/nickname")
    public JSendResponse updateUserNickname(@RequestHeader("Authorization") String authorizationHeader,
                                            @RequestBody UpdateNicknameRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.setNickname(accessToken, request.getNickname());
            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (RuntimeException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @GetMapping("api/v1/member/partner-image")
    public JSendResponse getPartnerProfileImage(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member self = memberService.findMemberByAccessToken(accessToken);
            Couple couple = coupleService.findCoupleById(self.getCoupleId());
            Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("url", partner.getMemberImage().getUrl());
            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @GetMapping("api/v1/member/image")
    public JSendResponse getMyProfileImage(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member self = memberService.findMemberByAccessToken(accessToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("url", self.getMemberImage().getUrl());
            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @PostMapping("api/v1/member/user-image")
    public JSendResponse updateDefaultProfileImage(@RequestHeader("Authorization") String authorizationHeader,
                                                   UpdateProfileUserImageRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.updateProfileUserImage(accessToken, request.getUserProfile());

            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @PostMapping("api/v1/member/default-image")
    public JSendResponse updateUserProfileImage(@RequestHeader("Authorization") String authorizationHeader,
                                                @RequestBody UpdateProfileImageRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.updateProfileDeaultImage(accessToken, request.getDefaultProfile());


            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @GetMapping("api/v1/member")
    public JSendResponse getMyInfo(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member self = memberService.findMemberByAccessToken(accessToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("nickname", self.getNickname());
            jsonObject.put("snsType", self.getSnsType());
            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @DeleteMapping("api/v1/member")
    public JSendResponse deleteMyAccount(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.deleteMyAccount(accessToken);

            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @GetMapping("api/v1/member/partner")
    public JSendResponse getPartnerInfo(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member self = memberService.findMemberByAccessToken(accessToken);
            Couple couple = coupleService.findCoupleById(self.getCoupleId());
            Member partner = (couple.getSelf() == self) ? couple.getPartner() : couple.getSelf();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("nickname", partner.getNickname());
            jsonObject.put("snsType", partner.getSnsType());
            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @GetMapping("api/v1/member/config")
    public JSendResponse getConfigurationInfo(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member self = memberService.findMemberByAccessToken(accessToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("language", self.getMemberConfig().getLanguage());
            jsonObject.put("isLock", self.getMemberConfig().getLock());
//            jsonObject.put("version", self.getMember_config().getVersion());
            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }


//    @GetMapping("api/v1/member/config/set")
//    public JSendResponse setConfig() {
//        try {
//            List<Member> all = memberService.findAll();
//            for(Member member : all) {
//                MemberConfig memberConfig = new MemberConfig();
//                memberConfig.setLanguage("korea");
//                memberConfig.setLock(false);
//                memberConfig.setVersion("1.0.0");
//                member.setMember_config(memberConfig);
//                memberService.join(member);
//            }
//            return new JSendResponse(Constants.HTTP_SUCCESS, null);
//        } catch (NullPointerException e) {
//            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
//
//        }
//    }

    @PutMapping("api/v1/member/config/language")
    public JSendResponse updateLanguage(@RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody UpdateLanguageRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member self = memberService.findMemberByAccessToken(accessToken);
            self.getMemberConfig().setLanguage(request.getLanguage());
            memberService.join(self);
            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @GetMapping("api/v1/member/config/password")
    public JSendResponse getPassword(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member member = memberService.findMemberByAccessToken(accessToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("password", member.getMemberConfig().getPassword());
            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @PostMapping("api/v1/member/config/password")
    public JSendResponse setPassword(@RequestHeader("Authorization") String authorizationHeader,
                                     @RequestBody UpdatePasswordRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.setPassword(accessToken, request.getPassword(), request.getQuestionType(), request.getAnswer());


            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (NullPointerException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @PutMapping("api/v1/member/config/password")
    public JSendResponse updatePassword(@RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody UpdatePasswordRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.updatePassword(accessToken, request.getPassword());


            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (RuntimeException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @PutMapping("api/v1/member/config/lock")
    public JSendResponse unlockPassword(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.unlockPassword(accessToken);

            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (RuntimeException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @GetMapping("api/v1/member/config/question-type")
    public JSendResponse getLockResetQuestion(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            MemberConfig config = memberService.getPasswordData(accessToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("questionType", config.getQuestionType());
            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @PostMapping("api/v1/member/config/check-password")
    public JSendResponse checkPassword(@RequestHeader("Authorization") String authorizationHeader,
                                       @RequestBody UpdatePasswordRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.checkPassword(accessToken, request.getPassword());

            return new JSendResponse(Constants.HTTP_SUCCESS, null);
        } catch (RuntimeException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    @PostMapping("api/v1/member/config/valid-answer")
    public JSendResponse validateLockResetAnswer(@RequestHeader("Authorization") String authorizationHeader,
                                                 @RequestBody ValidAnswerRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            boolean validAnswer = memberService.validAnswer(accessToken, request.getAnswer());
            boolean answer = validAnswer;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("isValid", validAnswer);
            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }

    /***
     * 닉네임 수정 횟수 초과 체크
     * @param authorizationHeader
     * @return
     */
    @GetMapping("api/v1/member/check-nickname-change-exceed")
    public JSendResponse checkNicknameChangeExceed(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Boolean exceed = memberService.checkNicknameChangeExceed(accessToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("exceed", exceed);
            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());

        }
    }


    @Data
    static class ChangeStatusRequest {
        private Boolean isInChat;
    }

    @Data
    static class UpdateLanguageRequest {
        private String Language;
    }

    @Data
    static class TestRequest {
        private MultipartFile image;
    }

    @Data
    static class UpdateProfileImageRequest {
        private Long defaultProfile;
    }

    @Data
    static class UpdateProfileUserImageRequest {
        private MultipartFile userProfile;
    }

    @Data
    static class UpdateFcmTokenRequest {
        private String fcmToken;
    }

    @Data
    static class LockResetQuestionRequest {
        private String questionType;
    }

    @Data
    static class UpdateNicknameRequest {
        private String nickname;
    }

    @Data
    static class ValidAnswerRequest {
        private String answer;
    }

    @Data
    static class UpdatePasswordRequest {
        private String password;
        private String questionType;
        private String answer;
    }
}