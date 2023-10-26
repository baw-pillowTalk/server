package com.fgama.pillowtalk.auth;

import com.fgama.pillowtalk.domain.Member;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;


/**
 * - 인증된 객체(인증 객체) 저장을 위한 클래스 : UsernamePasswordAuthenticationToken -> AbstractAuthenticationToken -> Authentication(Interface)
 */

@Getter
public class MemberAuthentication extends AbstractAuthenticationToken {

    private final String memberOauthId;

    public MemberAuthentication(Member member) {
        super(getAuthorities(member));
        this.memberOauthId = member.getUniqueId();
    }

    private static List<GrantedAuthority> getAuthorities(Member member) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.memberOauthId;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
