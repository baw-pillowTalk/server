package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.domain.PillowTalkBoard;
import com.fgama.pillowtalk.repository.MemberRepository;
import com.fgama.pillowtalk.repository.PillowTalkBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PillowTalkBoardService {
    private final PillowTalkBoardRepository pillowTalkBoardRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;


    public void addComment(String accessToken, String title, String body, String email) {
        Member member = this.memberService.getCurrentMember();
        PillowTalkBoard pillowTalkBoard = new PillowTalkBoard();
        if (title == null) {
            pillowTalkBoard.setTitle("질문 제안");
        }
        pillowTalkBoard.setTitle(title);
        pillowTalkBoard.setBody(body);
        pillowTalkBoard.setEmail(email);
        pillowTalkBoard.setCreator(member.getOauthId());
        pillowTalkBoard.setCreateAt(LocalDateTime.now());
        pillowTalkBoardRepository.save(pillowTalkBoard);
    }
}