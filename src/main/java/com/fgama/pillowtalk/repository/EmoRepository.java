package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.ChattingMessage;
import com.fgama.pillowtalk.domain.ChattingRoom;
import com.fgama.pillowtalk.domain.Emo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmoRepository extends JpaRepository<Emo, Long> {


   Optional<Emo> findOptionEmoByTitle(String title);
}
