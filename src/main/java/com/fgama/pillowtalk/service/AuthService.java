package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.auth.MemberAuthentication;
import com.fgama.pillowtalk.dto.auth.OauthLoginRequestDto;
import com.fgama.pillowtalk.dto.auth.OauthLoginResponseDto;
import com.fgama.pillowtalk.exception.auth.UnauthorizedMemberException;
import com.fgama.pillowtalk.exception.member.MemberNotFoundException;
import com.fgama.pillowtalk.repository.MemberRepository;
import com.fgama.pillowtalk.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final CoupleService coupleService;
    private final JwtService jwtService;

    /**
     * - 로그인
     **/
    @Transactional
    public OauthLoginResponseDto login(OauthLoginRequestDto request) {
        return this.processOauthLogin(this.getMemberFromOauthLoginRequest(request));
    }


    /**
     * - 로그 아웃
     **/
    @Transactional
    public Long logout() {
        Member member = this.memberService.getCurrentMember();
        // fcm null 설정
        member.logout();
        member.deleteRefreshToken();
        SecurityUtil.clearSecurityContext();
        return member.getId();
    }

    /**
     * - access token 재발급
     **/
    @Transactional
    public OauthLoginResponseDto reissue(HttpServletRequest httpServletRequest) {
        Member member = this.checkValidationForReissue(httpServletRequest);
        return this.processOauthLogin(member);
    }

    /**
     * - 회원 탈퇴
     **/
    @Transactional
    public void withDraw() {
        Member member = this.getCurrentMember(); // 1
        Couple couple = this.coupleService.getCouple(member); // 2
        this.coupleService.deleteCouple(couple); // Couple, CoupleChallenge, CoupleQuestion Soft Delete
        this.memberRepository.delete(member);
    }


    private Member getMemberFromOauthLoginRequest(OauthLoginRequestDto request) {
        Member member;
        try {
            // 자식 tx 에서 예외 발생 시 rollback = true 로 설정해서 해당 메소드에서 예외 발생
            member = this.memberService.findMemberByOauthIdAndSnsType(request.getOauthId(), request.getSnsType());
        } catch (MemberNotFoundException exception) {
            /* oauthId 해당하는 회원 존재 x */
            member = this.memberRepository.save(request.toEntity());
        }
        return member;
    }

    private OauthLoginResponseDto processOauthLogin(Member member) {
        SecurityContextHolder.getContext().setAuthentication(new MemberAuthentication(member));
        OauthLoginResponseDto serviceToken = this.jwtService.createServiceToken(member);
        member.setRefreshToken(serviceToken.getRefreshToken());
        return serviceToken;
    }

    private Member getMemberByToken(String accessToken) {
        Long id = Long.valueOf(this.jwtService.extractSubjectFromAccessToken(accessToken));
        return this.memberService.getMemberById(id);
    }


    /**
     * - access token + refresh token 재발급 유효성 검증을 위한 메소드
     **/
    private Member checkValidationForReissue(HttpServletRequest httpServletRequest) {

        Optional<String> optionalAccessToken = this.jwtService.extractAccessTokenFromRequest(httpServletRequest);
        Optional<String> optionalRefreshToken = this.jwtService.extractRefreshTokenFromRequest(httpServletRequest);

        // 1. Authorization header or Authorization-refresh header 가 없는 경우
        if (optionalAccessToken.isEmpty() || optionalRefreshToken.isEmpty()) {
            throw new UnauthorizedMemberException("요청 헤더에 jwt 정보가 부족합니다.");
        }

        String refreshToken = optionalRefreshToken.get();
        String accessToken = optionalAccessToken.get();

        // 2. refresh token 유효성 검증
        if (!this.jwtService.validateToken(refreshToken)) {
            throw new UnauthorizedMemberException("재로그인이 필요합니다.");
        }

        // 3. member refresh token 과 일치 여부 확인
        Member member = this.getMemberByToken(accessToken);
        if (!Objects.equals(member.getRefreshToken(), refreshToken)) {
            throw new UnauthorizedMemberException("Refresh Token 값이 일치하지 않습니다.");
        }
        return member;
    }

    private Member getCurrentMember() {
        return this.memberService.getCurrentMember();
    }
}
