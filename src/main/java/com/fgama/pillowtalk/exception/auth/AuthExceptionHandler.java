package com.fgama.pillowtalk.exception.auth;

import com.fgama.pillowtalk.api.AuthController;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.exception.member.MemberNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AuthController.class)
@Slf4j
public class AuthExceptionHandler {
    /* 인증 실패 회원 오류 */
    @ExceptionHandler(UnauthorizedMemberException.class)
    public ResponseEntity<JSendResponse> unauthorizedMemberExceptionHandler(
            UnauthorizedMemberException exception
    ) {
        log.warn("UnauthorizedMemberException Occurs");
        return ResponseEntity.ok(JSendResponse.of(exception));
    }

    /* 회원 존재 x 오류 */
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<JSendResponse> memberNotFoundExceptionHandler(
            MemberNotFoundException exception
    ) {
        log.warn("MemberNotFoundException Occurs");
        return ResponseEntity.ok(JSendResponse.of(exception));
    }
}
