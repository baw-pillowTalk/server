package com.fgama.pillowtalk.util;

import com.fgama.pillowtalk.auth.MemberAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

/**
 * SecurityContext 에 저장된 MemberAuthentication 관련 Util
 */

@Slf4j
@RequiredArgsConstructor
public class SecurityUtil {

    /* 현재 로그인한 회원의 uerId */
    public static Long getMemberId() {
        MemberAuthentication memberAuthentication
                = (MemberAuthentication) SecurityContextHolder.getContext().getAuthentication();
        return memberAuthentication.getUserId();
    }

    /* 현재 로그인한 회원의 권한 */
    public static Collection<GrantedAuthority> getMemberAuthorities() {
        MemberAuthentication memberAuthentication
                = (MemberAuthentication) SecurityContextHolder.getContext().getAuthentication();
        return memberAuthentication.getAuthorities();
    }

    /* SecurityContext 초기화 */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
