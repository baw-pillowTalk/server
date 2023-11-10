package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.dto.couple.MatchCoupleRequestDto;
import com.fgama.pillowtalk.service.CoupleQuestionService;
import com.fgama.pillowtalk.service.CoupleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Long> createCouple(
            @RequestBody @Valid MatchCoupleRequestDto request
    ) {
        return new ResponseEntity<>(this.coupleService.createCouple(request), HttpStatus.CREATED);
    }
}