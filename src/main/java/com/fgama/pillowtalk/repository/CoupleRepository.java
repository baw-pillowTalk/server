package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoupleRepository extends JpaRepository<Couple, Long> {

    public Couple findCoupleById(Long id);
    Optional<Couple> findOptionalCoupleById(Long id);

    @Query("select m from Couple m where m.coupleCode = :coupleCode")
    public List<Couple> findMatedCouples(@Param("coupleCode") String coupleCode);
    public Couple findCoupleByCoupleCode(String coupleCode);
    public void deleteCoupleByCoupleCode(String coupleCode);
}
