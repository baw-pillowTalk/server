package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.constant.HttpResponse;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.dto.member.*;
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

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final CoupleService coupleService;
    private final ChallengeService challengeService;
    private final CoupleQuestionService coupleQuestionService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;


    /**
     * case 01) 처음 애플리케이션을 구동 시켰을 때, 가장 먼저 해당 API 호출
     * - 호출 시 프론트에서, access, refresh 체크 후, 재 발급 필요 하다면 재 발급 API 요청
     * - 호출 시 프론트에서, access, refresh 체크 후, 재 발급 필요 없다면 auto-login API 요청
     * -> 회원 상태 가져올 수 있음
     * case 02) 비회원인 경우
     * - 비회원인 경우 아무 것도 없을 것 -> 프론트에서 login API 호출 -> 로그인 후, case 01) 을 따름
     **/
    @GetMapping("/api/v1/member/status")
    public JSendResponse getMemberStatus() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("memberStatus", this.memberService.getMemberStatus());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /**
     * - 회원 초대 코드 가져오기 API
     **/
    @GetMapping("/api/v1/member/invite-code")
    public JSendResponse getInviteCode() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("inviteCode", this.memberService.getInviteCode());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }


    /***
     * 채팅방 상태 변경
     * 유저가 채팅을 읽고 있는 상태 or 읽지 않는 상태 변경
     * 변경 후 파트너 에게 공지
     */
    @PatchMapping("/api/v1/member/chatting-room-status")
    public JSendResponse changeChatRoomStatus(
            @Valid @RequestBody ChangeChattingRoomStateRequestDto request
    ) {
        JSONObject jsonObject = new JSONObject();
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /***
     * 탈퇴 직전 유저가 가진 데이터
     */
    @GetMapping("/api/v1/member/service-data")
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

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (NullPointerException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());

        }
    }

    /**
     * - fcm 변경 API
     **/
    @PatchMapping("/api/v1/member/fcm-token")
    public JSendResponse updateFcmToken(
            @RequestBody @Valid UpdateFcmTokenRequestDto request
    ) {
        this.memberService.updateFcmToken(request);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
    }

    /**
     * - nickname 변경 API
     **/
    @PatchMapping("/api/v1/member/nickname")
    public JSendResponse updateUserNickname(
            @RequestBody @Valid UpdateNicknameRequestDto request) {
        this.memberService.updateNickname(request);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
    }

    /**
     * - 파트너 이미지 가져오기 API
     **/
    @GetMapping("/api/v1/member/partner-image")
    public JSendResponse getPartnerProfileImage() {
        GetProfileImageResponseDto partnerProfileImage = this.memberService.getPartnerProfileImage();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName", partnerProfileImage.getFileName());
        jsonObject.put("imagePath", partnerProfileImage.getImagePath());
        jsonObject.put("url", partnerProfileImage.getUrl());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /**
     * - 회원 이미지 가져오기 API
     **/
    @GetMapping("/api/v1/member/image")
    public JSendResponse getMyProfileImage() {
        GetProfileImageResponseDto myProfileImage = this.memberService.getMyProfileImage();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName", myProfileImage.getFileName());
        jsonObject.put("imagePath", myProfileImage.getImagePath());
        jsonObject.put("url", myProfileImage.getUrl());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /**
     * - 회원 이미지 수정 하기 API
     **/
    @PutMapping("/api/v1/member/image")
    public JSendResponse updateMyProfileImage(
            @RequestBody @Valid UpdateProfileImageRequestDto request
    ) {
        this.memberService.updateMyProfileImage(request);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
    }

    /**
     * - 회원 이미지 기본 이미지로 변경 API
     **/
    @PutMapping("/api/v1/member/default-image/{default_image}")
    public JSendResponse updateUserProfileImage(
            @PathVariable(name = "default_image") Long defaultImage) {
        this.memberService.updateToDefaultImage(defaultImage);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
    }

    /**
     * - 회원 정보 가져오기 API
     **/
    @GetMapping("/api/v1/member")
    public JSendResponse getMemberInfo() {
        GetMemberInfoResponseDto memberInfo = this.memberService.getMemberInfo();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", memberInfo.getNickName());
        jsonObject.put("snsType", memberInfo.getSnsType());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }


    /**
     * - 회원 파트너 정보 가져오기
     **/
    @GetMapping("/api/v1/member/partner")
    public JSendResponse getPartnerInfo() {
        GetPartnerInfoResponseDto partnerInfo = this.memberService.getPartnerInfo();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", partnerInfo.getNickName());
        jsonObject.put("snsType", partnerInfo.getSnsType());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /**
     * - 회원 설정(Config) 값 가져오기
     **/
    @GetMapping("/api/v1/member/config")
    public JSendResponse getConfigurationInfo() {
        GetMemberConfigInfoResponseDto memberConfig = this.memberService.getMemberConfig();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("language", memberConfig.getLanguage());
        jsonObject.put("lock", memberConfig.getLock());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /**
     * - 회원 언어 설정 API
     **/
    @PutMapping("/api/v1/member/config/language")
    public JSendResponse setMemberLanguage(
            @Valid @RequestBody SetMemberLanguageRequestDto request) {
        this.memberService.setMemberLanguage(request);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
    }

    /**
     * - 회원 비밀번호 가져오기 API
     **/
    @GetMapping("/api/v1/member/config/password")
    public JSendResponse getPassword() {
        String memberPassword = this.memberService.getMemberPassword();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("password", memberPassword);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /**
     * - 회원 비밀번호 설정하기 API
     **/
    @PostMapping("/api/v1/member/config/password")
    public JSendResponse setMemberPassword(
            @Valid @RequestBody SetMemberPasswordRequestDto request) {
        this.memberService.setMemberPassword(request);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
    }

    /**
     * - 회원 비밀번호 수정 하기 API
     **/
    @PatchMapping("/api/v1/member/config/password")
    public JSendResponse updatePassword(
            @Valid @RequestBody UpdateMemberPasswordRequestDto request) {
        this.memberService.updateMemberPassword(request);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
    }


    /**
     * - 잠금 해제
     **/
    @PutMapping("/api/v1/member/config/unlock")
    public JSendResponse unlockPassword() {
        try {
            this.memberService.unlockPassword();
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());
        }
    }

    /**
     * - 회원의 답변 질문 가져오기
     **/
    @GetMapping("/api/v1/member/config/question-type")
    public JSendResponse getLockResetQuestion() {
        int memberQuestionType = this.memberService.getMemberQuestionType();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("questionType", memberQuestionType);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /**
     * - 화면 잠금 비밀번호 값 맞는지 체크
     **/
    @PostMapping("/api/v1/member/config/check-password")
    public JSendResponse checkPassword(
            @Valid @RequestBody UpdatePasswordRequestDto request) {
        this.memberService.checkPassword(request);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
    }

    /**
     * - 회원 답변에 대한 질문 체크
     **/
    @PostMapping("/api/v1/member/config/valid-answer")
    public JSendResponse validateLockResetAnswer(
            @Valid @RequestBody CheckMemberAnswerValidationRequestDto request) {
        boolean result = this.memberService.validAnswer(request);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", result);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }


    /***
     * 닉네임 수정 횟수 초과 체크
     * @param authorizationHeader
     * @return
     */
    @GetMapping("/api/v1/member/check-nickname-change-exceed")
    public JSendResponse checkNicknameChangeExceed(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Boolean exceed = memberService.checkNicknameChangeExceed(accessToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("exceed", exceed);
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());

        }
    }

    /**
     * 나의 시그널 가져오기
     **/
    @GetMapping("/api/v1/member/signal")
    public JSendResponse getMySignal() {
        GetSignalInfoResponseDto mySignal = this.memberService.getMySignal();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mySignal", mySignal.getSignal());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }

    /**
     * 파트너 시그널 가져오기
     **/
    @GetMapping("/api/v1/member/partner/signal")
    public JSendResponse getPartnerSignal() {
        GetSignalInfoResponseDto partnerSignal = this.memberService.getPartnerSignal();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("partnerSignal", partnerSignal.getSignal());
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);

    }

    /**
     * 내 시그널 수정 하기
     **/
    @PostMapping("/api/v1/member/signal")
    public JSendResponse updateMySignal(
            @Valid @RequestBody UpdateMySignalRequestDto request) {
        this.memberService.updateMemberSignal(request);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, null);
    }


    @Data
    static class TestRequest {
        private MultipartFile image;
    }


    @Data
    static class LockResetQuestionRequest {
        private String questionType;
    }

    @Data
    static class ValidAnswerRequest {
        private String answer;
    }
}