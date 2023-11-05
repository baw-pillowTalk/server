package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.auth.OauthLoginResponseDto;
import com.fgama.pillowtalk.exception.auth.UnauthorizedMemberException;
import com.fgama.pillowtalk.properties.JwtProperties;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * - 1. access token 생성
 * - 2. refresh toke 생성
 * - 3. Service Token 생성
 * - 4. access & refresh Token 파싱
 * - 5. token 에서 subject claims 파싱
 * - 6. jwt 유효성 검증
 **/

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtService {
    private final JwtProperties jwtProperties;

    /**
     * 1. access token 생성
     **/
    public String createAccessToken(String payLoad) {
        return this.createToken(payLoad, jwtProperties.getAccessExpiration());
    }

    /**
     * 2. refresh token 생성
     */
    public String createRefreshToken() {
        return this.createToken(UUID.randomUUID().toString(), jwtProperties.getRefreshExpiration());
    }

    /**
     * 3. Service Token 생성
     **/
    public OauthLoginResponseDto createServiceToken(Member member) {
        return OauthLoginResponseDto.builder()
                .tokenType(this.jwtProperties.getBearer())
                .accessToken(this.createAccessToken(String.valueOf(member.getId())))
                .refreshToken(this.createRefreshToken())
                .expiredTime(LocalDateTime.now().plusSeconds(this.jwtProperties.getAccessExpiration()))
                .build();
    }


    /**
     * Token 으로부터 Subject Claim 파싱
     **/
    public String extractSubjectFromAccessToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            throw new UnauthorizedMemberException("로그인이 필요합니다.");
        }
    }

    /**
     * HttpServletRequest 로부터 Access Token 파싱
     */
    public Optional<String> extractAccessTokenFromRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(StringUtils::hasText)
                .filter(accessToken -> accessToken.startsWith(this.jwtProperties.getBearer()))
                .map(accessToken -> accessToken.substring(this.jwtProperties.getBearer().length()).trim());
    }

    /**
     * HttpServletRequest 로부터 Refresh Token 파싱
     **/
    public Optional<String> extractRefreshTokenFromRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(this.jwtProperties.getRefreshHeader()))
                .filter(StringUtils::hasText);
    }

    /**
     * Token 유효성 검증
     **/
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(jwtProperties.getSecretKey()).parseClaimsJws(token);
            return !claimsJws.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 에 오류가 존재합니다.");
        }
        return false;
    }

    private String createToken(String payLoad, Long tokenExpiration) {
        Claims claims = Jwts.claims().setSubject(payLoad);
        Date tokenExpiresIn = new Date(new Date().getTime() + tokenExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(tokenExpiresIn)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey())
                .compact();
    }
}
