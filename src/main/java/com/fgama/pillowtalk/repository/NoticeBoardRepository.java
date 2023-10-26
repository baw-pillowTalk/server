package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.NoticeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeBoardRepository extends JpaRepository<NoticeBoard, Long> {

    Page<NoticeBoard> findByCreator(String creator, PageRequest pageRequest);
}