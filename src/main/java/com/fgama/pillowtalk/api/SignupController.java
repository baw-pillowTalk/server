package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.dto.signup.CompleteMemberSignupRequestDto;
import com.fgama.pillowtalk.service.SignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 소셜 로그인 후 일부 나머지 회원 가입 기능을 위한 컨트롤러
 **/
@RestController
@Slf4j
@RequiredArgsConstructor
public class SignupController {
    private final SignupService signupService;

    /***
     * 회원가입
     * 닉네임 입력후 확인 누를시 동작
     * 각 snsType에 맞춰 사용자 정보 가져오기 후 사용자 정보를 이용하여 회원 생성
     */
    @PostMapping("/api/v1/signup")
    public ResponseEntity<Long> signup(
            @Valid @RequestBody CompleteMemberSignupRequestDto request
    ) {
        return new ResponseEntity<>(this.signupService.signup(request), HttpStatus.OK);
    }
}