package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.components.FileDetail;
import com.fgama.pillowtalk.domain.ChattingMessage;
import com.fgama.pillowtalk.domain.ChattingRoom;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.exception.MemberNotFoundException;
import com.fgama.pillowtalk.repository.ChattingMessageRepository;
import com.fgama.pillowtalk.repository.ChattingRoomRepository;
import com.fgama.pillowtalk.repository.CoupleRepository;
import com.fgama.pillowtalk.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChattingRoomService {
    private final ChattingRoomRepository chattingRoomRepository;
    private final ChattingMessageRepository chattingMessageRepository;
    private final MemberRepository memberRepository;
    private final CoupleRepository coupleRepository;
    private final FileUploadService fileUploadService;

    public Long join(ChattingRoom chattingRoom) {
//        validateDuplicateChattingRoom(chattingRoom);
        ChattingRoom chattingRoom1 = chattingRoomRepository.save(chattingRoom);
        return chattingRoom1.getId();
    }

    public void validateDuplicateChattingRoom(ChattingRoom chattingRoom) {
        List<ChattingRoom> findChattingRooms = chattingRoomRepository.findChattingRoomsByTitle(chattingRoom.getTitle());
        if (!findChattingRooms.isEmpty()) {
            throw new IllegalStateException("이미 존재 하는 채팅방 입니다");
        }
    }

    public List<ChattingRoom> findChattingRoomsByCoupleId(Long coupleId) {
        return chattingRoomRepository.findChattingRoomsByCoupleId(coupleId);
    }

    public ChattingRoom findChattingRoomsByCoupleIdAndTitle(Long coupleId, String title) {
        return chattingRoomRepository.findChattingRoomsByCoupleIdAndTitle(coupleId, title);
    }

    public int countByCoupleId(Long coupleId) throws IllegalStateException {
        int count = chattingRoomRepository.countByCoupleId(coupleId);
        if (count == 0) {
            throw new IllegalStateException("질문을 가지고있지 않습니다");
        }

        return count;
    }


    public Page<ChattingRoom> findNewest(Long coupleId, int pageNo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageNo, 4, sort);
        Page<ChattingRoom> rooms = chattingRoomRepository.findChattingRoomsByCoupleId(coupleId, pageRequest);

        return rooms;
    }

    public ChattingMessage addChattingMessage(String accessToken, String message, Long index, String emoji, String url, MultipartFile multipartFile, String type) throws NullPointerException {
        log.info("add chattingMessage accessToken: " + accessToken);
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        Couple couple = coupleRepository.findCoupleById(member.getCoupleId());

        Member partner = (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();

        log.info("add chattingMessage find member");
        ChattingMessage chattingMessage = new ChattingMessage();
        chattingMessage.setMessage(message);
//        chattingMessage.setMember(member);
        chattingMessage.setCreatedAt(LocalDateTime.now());
        chattingMessage.setIsRead(partner.getChattingRoomStatus());
        chattingMessage.setType(type);
        chattingMessage.setNumber((long) couple.getChattingRooms().get(0).getMessageList().size());
        chattingMessage.setChattingRoom(couple.getChattingRooms().get(0));
        log.info("add chattingMessage find chattingRoom");

        FileDetail fileDetail;
        switch (type) {
            case "text":
                break;
            case "voice":
                fileDetail = fileUploadService.save(multipartFile);
                chattingMessage.setResourceUrl("https://s3.ap-northeast-2.amazonaws.com/" + "pillow.images/" + fileDetail.getPath());
                break;
            case "image":
                fileDetail = fileUploadService.save(multipartFile);
                chattingMessage.setResourceUrl("https://s3.ap-northeast-2.amazonaws.com/" + "pillow.images/" + fileDetail.getPath());
                break;
            case "video":
                chattingMessage.setResourceUrl(url);
                break;
            case "question":
                chattingMessage.setQuestionIndex(index);
                break;
            case "challenge":
                chattingMessage.setChallengeIndex(index);
                break;
            case "emoji":
                chattingMessage.setEmoji(emoji);
                chattingMessage.setResourceUrl(url);
                break;
        }


        log.info("add chattingMessage done");
        return chattingMessageRepository.save(chattingMessage);
    }


}