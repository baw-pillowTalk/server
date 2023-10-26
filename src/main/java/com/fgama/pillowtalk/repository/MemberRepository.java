package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {


    Optional<Member> findOptionalMemberByState(String state);

    Optional<Member> findOptionalMemberByAccessToken(String accessToken);

    Optional<Member> findOptionalMemberByInviteCode(String code);

    Optional<Member> findMemberByAccessToken(String accessToken);

    Optional<Member> findMemberByRefreshToken(String refreshToken);

    Optional<Member> findOptionalMemberByUniqueId(String uniqueId);

    Member findMemberByUniqueId(String uniqueId);

    void deleteMemberByAccessToken(String accessToken);

}