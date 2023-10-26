package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.NoticeBoard;
import com.fgama.pillowtalk.repository.NoticeBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeBoardService {
    private final NoticeBoardRepository noticeBoardRepository;

    public Long join(NoticeBoard noticeBoard) {
        noticeBoardRepository.save(noticeBoard);
        return noticeBoard.getId();
    }

    public List<NoticeBoard> getNoticeBoardList(int pageNo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createAt");
        PageRequest pageRequest = PageRequest.of(pageNo, 4, sort);
        Page<NoticeBoard> all = noticeBoardRepository.findByCreator("master", pageRequest);

        return all.getContent();
    }


    public NoticeBoard getNoticeBoard(Long index) {
        return noticeBoardRepository.findById(index).orElseThrow(RuntimeException::new);
    }
}