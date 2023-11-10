package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.signup.CompleteMemberSignupRequestDto;
import com.fgama.pillowtalk.exception.signup.MemberStateNotEqualException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class SignupService {
    private final MemberService memberService;

    @Transactional
    public Long signup(CompleteMemberSignupRequestDto request) {
        Member member = this.memberService.getCurrentMember();
        if (request.getState() != null) {
            this.checkMemberState(member, request);
        }
        return member.completeMemberSignup(request);
    }

    private void checkMemberState(Member member, CompleteMemberSignupRequestDto request) {
        if (!Objects.equals(member.getState(), request.getState())) {
            throw new MemberStateNotEqualException("회원 상태가 일치하지 않습니다.");
        }
    }
}
