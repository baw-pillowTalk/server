package com.fgama.pillowtalk.exception.member;

import com.fgama.pillowtalk.api.MemberController;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.exception.couple.CoupleNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = MemberController.class)
public class MemberExceptionHandler {

    /* 회원 존재 x 오류 */
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<JSendResponse> memberNotFoundExceptionHandler(
            MemberNotFoundException exception
    ) {
        return ResponseEntity.ok(JSendResponse.of(exception));
    }

    /* 커플 존재 x 오류 */
    @ExceptionHandler(CoupleNotFoundException.class)
    public ResponseEntity<JSendResponse> coupleNotFoundExceptionHandler(
            CoupleNotFoundException exception
    ) {
        return ResponseEntity.ok(JSendResponse.of(exception));
    }

    /* 비밀번호 수정 시 기존 비밀번호 존재 x */
    @ExceptionHandler(PasswordNotFoundException.class)
    public ResponseEntity<JSendResponse> passwordNotFoundExceptionHandler(
            PasswordNotFoundException exception
    ) {
        return ResponseEntity.ok(JSendResponse.of(exception));
    }

    /* 비밀번호 설정 시 기존 비밀번호 존재 o */
    @ExceptionHandler(PasswordAlreadyExistException.class)
    public ResponseEntity<JSendResponse> passwordAlreadyExistExceptionHandler(
            PasswordAlreadyExistException exception
    ) {
        return ResponseEntity.ok(JSendResponse.of(exception));
    }

    /* 비밀번호 일치하지 x */
    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity<JSendResponse> passwordNotMatchExceptionHandler(
            PasswordNotMatchException exception
    ) {
        return ResponseEntity.ok(JSendResponse.of(exception));
    }
}
