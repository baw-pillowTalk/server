package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.service.CoupleQuestionService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * - 인증 부분 테스트를 위한 컨트롤러
 **/
@Slf4j
@RequiredArgsConstructor
@RestController
public class TestController {
    private final CoupleQuestionService coupleQuestionService;
    @GetMapping("test/addCoupleQuestion")
    public void addCoupleQuestion(){
        coupleQuestionService.addCoupleQuestion(1L);
    }
}
