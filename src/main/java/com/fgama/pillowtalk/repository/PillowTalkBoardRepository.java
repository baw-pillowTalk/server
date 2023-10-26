package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.NoticeBoard;
import com.fgama.pillowtalk.domain.PillowTalkBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PillowTalkBoardRepository extends JpaRepository<PillowTalkBoard, Long> {


}
