package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.signup.CompleteMemberSignupRequestDto;
import com.fgama.pillowtalk.exception.signup.MemberStateNotEqualException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Random;

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
        String inviteCode = this.makeInviteCode();
        return member.completeMemberSignup(inviteCode, request);
    }

    private void checkMemberState(Member member, CompleteMemberSignupRequestDto request) {
        if (!Objects.equals(member.getState(), request.getState())) {
            throw new MemberStateNotEqualException("회원 상태가 일치하지 않습니다.");
        }
    }

    private String makeInviteCode() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
