package com.fgama.pillowtalk.filter;

import com.fgama.pillowtalk.auth.SignInWithGoogle;
import com.fgama.pillowtalk.auth.SignInWithKakao;
import com.fgama.pillowtalk.auth.SignInWithNAVER;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
//@Component
public class CustomRequestUriFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestUri = request.getRequestURI();
        log.info("Request URI: {}", requestUri);
        log.info("필터 토큰검증 실행");
        try {
            log.info("필터 토큰검증 실행1");
            String header = request.getHeader("Authorization");
            log.info(header);
            String accessToken = header.substring(7);
            log.info(accessToken);

            log.info("filter 검증 시작" + accessToken);
            log.info("filter google검증 시작");
            Boolean googleSuccess = SignInWithGoogle.validAccessToken(accessToken);
            log.info("filter google : " + googleSuccess);
            Boolean kakaoSuccess = SignInWithKakao.validAccessToken(accessToken);
            log.info("filter kakao : " + kakaoSuccess);
            Boolean naverSuccess = SignInWithNAVER.validAccessToken(accessToken);
            log.info("filter naver : " + naverSuccess);
//            Boolean appleSuccess = SigninWithAppleJWT.validAccessToken(accessToken);
//            log.info("filter google : " + googleSuccess);
            if (googleSuccess || kakaoSuccess || naverSuccess) {
                log.info("필터 accessToken검증성공");

                List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
                Authentication authentication = new UsernamePasswordAuthenticationToken(null, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
            }

        } catch (RuntimeException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        } catch (ServletException e) {
            log.info(e.getMessage());
            throw e;
        } catch (IOException e) {
            log.info(e.getMessage());
            throw e;
        }

    }

}