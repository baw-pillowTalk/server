package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.Challenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findByCoupleId(Long coupleId);

    Challenge findByCoupleIdAndNumber(Long coupleId, Long Number);

    void deleteByCoupleIdAndNumber(Long coupleId, Long Number);

    List<Challenge> findByCoupleId(Long coupleId, PageRequest pageRequest);

    List<Challenge> findByCoupleIdAndDone(Long coupleId, Boolean done, PageRequest pageRequest);

    Page<Challenge> findPageByCoupleId(Long coupleId, PageRequest pageRequest);

    Page<Challenge> findPageByCoupleIdAndDone(Long coupleId, Boolean done, PageRequest pageRequest);
}