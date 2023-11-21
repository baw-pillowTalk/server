package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.components.AmazonS3ResourceStorage;
import com.fgama.pillowtalk.domain.chattingMessage.ChattingMessage;
import com.fgama.pillowtalk.domain.ChattingRoom;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.repository.ChattingMessageRepository;
import com.fgama.pillowtalk.repository.CoupleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChattingMessageService {
    private final ChattingMessageRepository chattingMessageRepository;
    private final AmazonS3ResourceStorage amazonS3ResourceStorage;
    private final CoupleRepository coupleRepository;

    private final MemberService memberService;

    public Long join(ChattingMessage chattingMessage) {
        ChattingMessage chattingMessage1 = chattingMessageRepository.save(chattingMessage);
        return chattingMessage1.getId();
    }

    public List<ChattingMessage> findByChattingRoom(ChattingRoom chattingRoom) {
        return chattingMessageRepository.findByChattingRoom(chattingRoom);
    }

    public void addMessage() {
        //todo
    }

    public List<ChattingMessage> loadChatList(String accessToken, int pageNo) throws NullPointerException {
        Member member = this.memberService.getCurrentMember();
        Couple couple = coupleRepository.findById(member.getCoupleId()).orElseThrow(NullPointerException::new);
        log.info(couple.getId().toString());
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageNo, 4, sort);
        Page<ChattingMessage> byChattingRoom = chattingMessageRepository.findByChattingRoom(couple.getChattingRoom(), pageRequest);
        return byChattingRoom.getContent();
    }

    public int getLatestChatPageNo(String accessToken) {
        Member member = this.memberService.getCurrentMember();
        Couple couple = coupleRepository.findById(member.getCoupleId()).orElseThrow(NullPointerException::new);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, 4, sort);
        Page<ChattingMessage> byChattingRoom = chattingMessageRepository.findByChattingRoom(couple.getChattingRoom(), pageRequest);
        if (byChattingRoom.getTotalPages() == 0) {
            return byChattingRoom.getTotalPages();
        } else {
            return byChattingRoom.getTotalPages() - 1;
        }
    }


//    public String getId(String accessToken,String fileName){
//        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
//        Couple couple = coupleRepository.findCoupleById(member.getCoupleId());
//
//        couple.getChattingRooms().get(0).
//
//
//        amazonS3ResourceStorage.getID(fileName);
//    }
}
