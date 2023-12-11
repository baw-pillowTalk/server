package com.fgama.pillowtalk.exception.couple;

import com.fgama.pillowtalk.api.CoupleController;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.exception.member.MemberNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = CoupleController.class)
@Slf4j
public class CoupleExceptionHandler {
    /* 회원 존재 x */
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<JSendResponse> memberNotFoundExceptionHandler(
            MemberNotFoundException exception
    ) {
        log.warn("MemberNofFoundException Occurs");
        return ResponseEntity.ok(JSendResponse.of(exception));
    }

    /* 커플 이미 존재 */
    @ExceptionHandler(CoupleAlreadyExistException.class)
    public ResponseEntity<JSendResponse> coupleAlreadyExistExceptionHandler(
            CoupleAlreadyExistException exception
    ) {
        log.warn("CoupleAlreadyExistException Occurs");
        return ResponseEntity.ok(JSendResponse.of(exception));
    }

    /* 커플 존재 x */
    @ExceptionHandler(CoupleNotFoundException.class)
    public ResponseEntity<JSendResponse> coupleNotFoundExceptionHandler(
            CoupleNotFoundException exception
    ) {
        log.warn("CoupleNotFoundException Occurs");
        return ResponseEntity.ok(JSendResponse.of(exception));
    }

    /* 커플 중 회원가입 미 완료 */
    @ExceptionHandler(CoupleNeedExtraSignupException.class)
    public ResponseEntity<JSendResponse> coupleNeedExtraSignupExceptionHandler(
            CoupleNeedExtraSignupException exception
    ) {
        log.warn("CoupleNeedExtraSignupException Occurs");
        return ResponseEntity.ok(JSendResponse.of(exception));
    }
}
