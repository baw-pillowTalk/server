package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.constant.HttpResponse;
import com.fgama.pillowtalk.constant.MemberStatus;
import com.fgama.pillowtalk.domain.MemberConfig;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<MemberStatus> getMemberStatus() {
        return ResponseEntity.ok(this.memberService.getMemberStatus());
    }

    /**
     * - 회원 초대 코드 가져오기 API
     **/
    @GetMapping("/api/v1/member/invite-code")
    public ResponseEntity<String> getInviteCode() {
        return ResponseEntity.ok(this.memberService.getInviteCode());
    }


    /***
     * 채팅방 상태 변경
     * 유저가 채팅을 읽고 있는 상태 or 읽지 않는 상태 변경
     * 변경 후 파트너 에게 공지
     */
    @PatchMapping("/api/v1/member/chatting-room-status")
    public ResponseEntity<Void> changeChatRoomStatus(
            @Valid @RequestBody ChangeChattingRoomStateRequestDto request
    ) {
        this.memberService.changeChatRoomStatus(request);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> updateFcmToken(
            @RequestBody @Valid UpdateFcmTokenRequestDto request
    ) {
        this.memberService.updateFcmToken(request);
        return ResponseEntity.ok().build();
    }

    /**
     * - nickname 변경 API
     **/
    @PatchMapping("/api/v1/member/nickname")
    public ResponseEntity<Void> updateUserNickname(
            @RequestBody @Valid UpdateNicknameRequestDto request) {
        this.memberService.updateNickname(request);
        return ResponseEntity.ok().build();
    }

    /**
     * - 파트너 이미지 가져오기 API
     **/
    @GetMapping("/api/v1/member/partner-image")
    public ResponseEntity<GetProfileImageResponseDto> getPartnerProfileImage() {
        return new ResponseEntity<>(this.memberService.getPartnerProfileImage(), HttpStatus.OK);
    }

    /**
     * - 회원 이미지 가져오기 API
     **/
    @GetMapping("/api/v1/member/image")
    public ResponseEntity<GetProfileImageResponseDto> getMyProfileImage() {
        return new ResponseEntity<>(this.memberService.getMyProfileImage(), HttpStatus.OK);
    }

    /**
     * - 회원 이미지 수정 하기 API
     **/
    @PutMapping("/api/v1/member/image")
    public ResponseEntity<Void> updateMyProfileImage(
            @RequestBody @Valid UpdateProfileImageRequestDto request
    ) {
        this.memberService.updateMyProfileImage(request);
        return ResponseEntity.ok().build();
    }

    /**
     * - 회원 이미지 기본 이미지로 변경 API
     **/
    @PutMapping("/api/v1/member/default-image/{default_image}")
    public ResponseEntity<Void> updateUserProfileImage(
            @PathVariable(name = "default_image") Long defaultImage) {
        this.memberService.updateToDefaultImage(defaultImage);
        return ResponseEntity.ok().build();
    }

    /**
     * - 회원 정보 가져오기 API
     **/
    @GetMapping("/api/v1/member")
    public ResponseEntity<GetMemberInfoResponseDto> getMemberInfo() {
        return ResponseEntity.ok().body(this.memberService.getMemberInfo());
    }


    /**
     * - 회원 파트너 정보 가져오기
     **/
    @GetMapping("/api/v1/member/partner")
    public ResponseEntity<GetPartnerInfoResponseDto> getPartnerInfo() {
        return ResponseEntity.ok().body(this.memberService.getPartnerInfo());
    }

    /**
     * - 회원 설정(Config) 값 가져오기
     **/
    @GetMapping("/api/v1/member/config")
    public ResponseEntity<GetMemberConfigInfoResponseDto> getConfigurationInfo() {
        return ResponseEntity.ok(this.memberService.getMemberConfig());
    }

    /**
     * - 회원 언어 설정 API
     **/
    @PutMapping("/api/v1/member/config/language")
    public ResponseEntity<Void> setMemberLanguage(
            @Valid @RequestBody SetMemberLanguageRequestDto request) {
        return ResponseEntity.ok(this.memberService.setMemberLanguage(request));
    }

    /**
     * - 회원 비밀번호 가져오기 API
     **/
    @GetMapping("/api/v1/member/config/password")
    public ResponseEntity<String> getPassword() {
        return ResponseEntity.ok(this.memberService.getMemberPassword());
    }

    /**
     * - 회원 비밀번호 설정하기 API
     **/
    @PostMapping("/api/v1/member/config/password")
    public ResponseEntity<Void> setMemberPassword(
            @Valid @RequestBody SetMemberPasswordRequestDto request) {
        return ResponseEntity.ok(this.memberService.setMemberPassword(request));
    }

    /**
     * - 회원 비밀번호 수정 하기 API
     **/
    @PatchMapping("/api/v1/member/config/password")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody UpdateMemberPasswordRequestDto request) {
        return ResponseEntity.ok(this.memberService.updateMemberPassword(request));
    }

    @PutMapping("/api/v1/member/config/lock")
    public JSendResponse unlockPassword(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.unlockPassword(accessToken);

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());

        }
    }

    @GetMapping("/api/v1/member/config/question-type")
    public JSendResponse getLockResetQuestion(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            MemberConfig config = memberService.getPasswordData(accessToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("questionType", config.getQuestionType());
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());

        }
    }

    @PostMapping("/api/v1/member/config/check-password")
    public JSendResponse checkPassword(@RequestHeader("Authorization") String authorizationHeader,
                                       @RequestBody UpdatePasswordRequestDto request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            memberService.checkPassword(accessToken, request.getPassword());

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());

        }
    }

    @PostMapping("/api/v1/member/config/valid-answer")
    public JSendResponse validateLockResetAnswer(@RequestHeader("Authorization") String authorizationHeader,
                                                 @RequestBody ValidAnswerRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            boolean validAnswer = memberService.validAnswer(accessToken, request.getAnswer());
            boolean answer = validAnswer;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("isValid", validAnswer);
            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());

        }
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
    public ResponseEntity<GetSignalInfoResponseDto> getMySignal() {
        return ResponseEntity.ok(this.memberService.getMySignal());
    }

    /**
     * 파트너 시그널 가져오기
     **/
    @GetMapping("/api/v1/member/partner/signal")
    public ResponseEntity<GetSignalInfoResponseDto> getPartnerSignal() {
        return ResponseEntity.ok(this.memberService.getPartnerSignal());
    }

    /**
     * 내 시그널 수정 하기
     **/
    @PostMapping("/api/v1/member/signal")
    public ResponseEntity<Void> updateMySignal(
            @Valid @RequestBody UpdateMySignalRequestDto request) {
        return ResponseEntity.ok(this.memberService.updateMemberSignal(request));
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

    @Data
    static class UpdatePasswordRequestDto {
        private String password;
        private String questionType;
        private String answer;
    }
}