package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.constant.HttpResponse;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.dto.couple.MatchCoupleRequestDto;
import com.fgama.pillowtalk.service.CoupleQuestionService;
import com.fgama.pillowtalk.service.CoupleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CoupleController {
    private final CoupleService coupleService;
    private final CoupleQuestionService coupleQuestionService;

    /**
     * - inviteCode 을 통해 couple 매칭 API
     * - inviteCode 을 받은 회원 기준 API
     **/
    @PostMapping("/api/v1/couple")
    public JSendResponse createCouple(
            @RequestBody @Valid MatchCoupleRequestDto request
    ) {
        Long couple = this.coupleService.createCouple(request);
        coupleQuestionService.addCoupleQuestion(couple);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("coupleId", couple);
        return new JSendResponse(HttpResponse.HTTP_SUCCESS, null, jsonObject);
    }
}