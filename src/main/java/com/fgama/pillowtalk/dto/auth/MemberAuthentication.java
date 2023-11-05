package com.fgama.pillowtalk.dto.auth;

import com.fgama.pillowtalk.domain.Member;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class MemberAuthentication extends AbstractAuthenticationToken {

    private final Long id;

    public MemberAuthentication(Member member) {
        super(getAuthorities(member));
        this.id = member.getId();
    }

    private static Collection<? extends GrantedAuthority> getAuthorities(Member member) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(member.getRole().name()));
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return id;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
