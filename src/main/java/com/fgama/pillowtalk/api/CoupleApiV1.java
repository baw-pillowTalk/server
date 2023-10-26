package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.config.Constants;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CoupleApiV1 {
    private final CoupleService coupleService;
    private final MemberService memberService;
    private final ChattingRoomService chattingRoomService;
    private final QuestionService questionService;
    private final CoupleQuestionService coupleQuestionService;


    @PostMapping("/api/v1/couple")
    public JSendResponse createCouple(@RequestHeader("Authorization") String authorizationHeader,
                                      @RequestBody CoupleMatchRequest request) {
        try {
            // 데이터불러오기
            String accessToken = authorizationHeader.substring("Bearer ".length());
            Member self = memberService.findMemberByAccessToken(accessToken);
            Member partner = memberService.findOptionalMemberByInviteCode(request.getInviteCode()); // RuntimeException
            //커플생성
            Couple couple = coupleService.createCouple(self, partner);
            coupleQuestionService.addCoupleQuestion(couple.getId());
            //기존에 있던 커플인지 확인하기 + 회원탈퇴시 커플데이터는유지

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("coupleId", couple.getId());

            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (RuntimeException e) {
            log.info("커플등록 에러" + e);
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    @Data
    static class CoupleMatchRequest {
        private String inviteCode;
    }


}