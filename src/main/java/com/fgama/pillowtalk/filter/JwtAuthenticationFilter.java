package com.fgama.pillowtalk.filter;


import com.fgama.pillowtalk.auth.MemberAuthentication;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.repository.MemberRepository;
import com.fgama.pillowtalk.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * - JWT 기반 인증 방식의 핵심이 되는 인증 필터
 * # API 요청에 있어서 JWT 가 요청 헤더에 담겨서 올 때, 유효성 검증 진행하여 검증 성공 및 검증 실패 처리
 * - 토큰의 유효성 만료 or 유효하지 않은 경우 : AuthenticationException
 * - 사용자 정보 존재하지 않는 경우 : UsernameNotFoundException
 * - 토큰 자체가 존재하지 않은 경우 : AuthenticationCredentialNotFoundException
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getMethod().equals("OPTIONS")) {
            return;
        }

        if (request.getRequestURI().equals("/api/v1/member/status")) {
            // /member/status
        }

        try {
            /* 1. HttpServletRequest 요청에서 access token 파싱 */
            String accessToken = this.jwtService.extractAccessTokenFromRequest(request)
                    .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("No Access Token is exist"));

            /* 2. access token 에서 unique claim 가져와서 Member 조회 */
            Member member = this.memberRepository.findById(Long.parseLong(this.jwtService.extractUniqueClaimFromPayload(accessToken)))
                    .orElseThrow(() -> new UsernameNotFoundException("No Member is exist"));

            /* 3. SecurityContext 에 AbstractAuthenticationToken 을 상속한 MemberAuthentication 저장 */
            MemberAuthentication memberAuthentication = new MemberAuthentication(member);
            SecurityContextHolder.getContext().setAuthentication(memberAuthentication);
        } catch (AuthenticationException exception) {
            log.info("JwtAuthentication UnauthorizedUserException!");
        }
        filterChain.doFilter(request, response);
    }
}