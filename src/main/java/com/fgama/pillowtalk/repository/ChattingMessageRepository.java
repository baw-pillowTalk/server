package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.ChattingMessage;
import com.fgama.pillowtalk.domain.ChattingRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChattingMessageRepository extends JpaRepository<ChattingMessage, Long> {


   List<ChattingMessage> findByChattingRoom(ChattingRoom chattingRoom);
   ChattingMessage findFirstByMemberIdOrderByCreatedAtDesc(Long id);

   Page<ChattingMessage> findByChattingRoom(ChattingRoom chattingRoom, PageRequest pageRequest);
}
