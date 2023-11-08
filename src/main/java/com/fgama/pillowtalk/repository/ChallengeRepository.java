package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.CoupleChallenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<CoupleChallenge, Long> {

    List<CoupleChallenge> findByCoupleId(Long coupleId);

    CoupleChallenge findByCoupleIdAndNumber(Long coupleId, int Number);

    void deleteByCoupleIdAndNumber(Long coupleId, int Number);

    List<CoupleChallenge> findByCoupleId(Long coupleId, PageRequest pageRequest);

    List<CoupleChallenge> findByCoupleIdAndDone(Long coupleId, Boolean done, PageRequest pageRequest);

    Page<CoupleChallenge> findPageByCoupleId(Long coupleId, PageRequest pageRequest);

    Page<CoupleChallenge> findPageByCoupleIdAndDone(Long coupleId, Boolean done, PageRequest pageRequest);
}