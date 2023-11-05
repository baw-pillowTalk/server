package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.constant.SnsType;
import com.fgama.pillowtalk.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findMemberByInviteCode(String code);

//    Optional<Member> findMemberByAccessToken(String accessToken);

    Optional<Member> findMemberByRefreshToken(String refreshToken);

    /**
     * oauthId 로 회원 조회
     **/
    Optional<Member> findMemberByOauthIdAndSnsType(String oauthId, SnsType snsType);
}