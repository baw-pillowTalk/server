package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.ChattingMessage;
import com.fgama.pillowtalk.domain.ChattingRoom;
import com.fgama.pillowtalk.domain.CoupleQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoupleQuestionRepository extends JpaRepository<CoupleQuestion, Long> {
    List<CoupleQuestion> findByCoupleId(Long coupleId);

    List<CoupleQuestion> findByCoupleId(Long coupleId, PageRequest pageRequest);

    CoupleQuestion findByNumberAndCoupleId(int number , Long coupleId);

    Page<CoupleQuestion> findPageByCoupleId(Long coupleId, PageRequest pageRequest);
}
