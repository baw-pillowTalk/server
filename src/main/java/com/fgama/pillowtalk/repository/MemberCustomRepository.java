package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.Member;

public interface MemberCustomRepository {
    Member save(Member member);
}
