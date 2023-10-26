package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.ChattingRoom;
import com.fgama.pillowtalk.domain.Couple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ChattingRoomRepository extends JpaRepository<ChattingRoom, Long> {


    List<ChattingRoom> findChattingRoomsByCoupleId(Long coupleId);
    Page<ChattingRoom> findChattingRoomsByCoupleId(Long coupleId , PageRequest pageRequest);
    List<ChattingRoom> findChattingRoomsByTitle(String title);


    int countByCoupleId(Long coupleId);
//    @Query("SELECT COUNT FROM ChattingRoom cr WHERE cr.coupleId = :coupleId")
//    int getChattingRoomCount(@Param("coupleId") Long coupleId);

    ChattingRoom findChattingRoomsByCoupleIdAndTitle(Long coupleId, String title);

}
