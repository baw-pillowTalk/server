package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.ChattingRoom;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.dto.couple.MatchCoupleRequestDto;
import com.fgama.pillowtalk.exception.couple.CoupleAlreadyExistException;
import com.fgama.pillowtalk.exception.couple.CoupleNotFoundException;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.repository.ChattingRoomRepository;
import com.fgama.pillowtalk.repository.CoupleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.fgama.pillowtalk.constant.CoupleStatus.AVAILABLE;
import static com.fgama.pillowtalk.constant.MemberStatus.COUPLE;


@Service
@RequiredArgsConstructor
public class CoupleService {
    private final CoupleRepository coupleRepository;
    private final ChattingRoomRepository chattingRoomRepository;

    private final FirebaseCloudMessageService firebaseCloudMessageService;
    private final MemberService memberService;

    public Long join(Couple couple) {
        Couple couple1 = coupleRepository.save(couple);
        return couple1.getId();
    }

    /**
     * - 초대 코드를 받은 self 와 보낸 partner couple 매칭
     * # Member Entity MemberStatus 변경 & coupleId 세팅
     **/
    @Transactional
    public Long createCouple(MatchCoupleRequestDto request) throws RuntimeException {

        Member self = this.memberService.getCurrentMember();
        Member partner = this.memberService.findMemberByInviteCode(request.getInviteCode());

        if (this.isCouple(self, partner)) {
            throw new CoupleAlreadyExistException("커플이 이미 존재합니다.");
        }
        /* couple 생성 */
        Couple couple = Couple.builder()
                .self(self)
                .partner(partner)
                .coupleStatus(AVAILABLE)
                .build();

        /* chatting room 생성 */
        this.chattingRoomRepository.save(ChattingRoom.builder()
                .title("커플 채팅방")
                .couple(couple)
                .build());

        self.setCoupleId(couple.getId());
        self.setMemberStatus(COUPLE);
        partner.setCoupleId(couple.getId());
        partner.setMemberStatus(COUPLE);

        String fcmDetail = firebaseCloudMessageService.getFcmJsonObject("필로우에 오신걸 환영해요\uD83D\uDD13", "createCouple", "두 분의 커플 매칭이 완료 되었어요!");
        this.firebaseCloudMessageService.sendFcmMessage(fcmDetail, partner.getFcmToken());
        return couple.getId();
    }

    private Boolean isCouple(Member self, Member partner) {
        return (self.getMemberStatus().equals(COUPLE) || partner.getMemberStatus().equals(COUPLE)
                || self.getCoupleId() != null || partner.getCoupleId() != null);
    }


    /**
     * - 현재 로그인한 회원의 파트너 가져오기
     **/
    public Member getCouplePartner() {
        Member member = this.memberService.getCurrentMember();
        Couple couple = this.getCouple(member);
        return (couple.getSelf() == member) ? couple.getPartner() : couple.getSelf();
    }

    /**
     * - 현재 로그인한 회원의 Couple 정보 가져오기
     **/
    public Couple getCouple(Member member) {
        return this.coupleRepository.findCoupleById(member.getCoupleId())
                .orElseThrow(() -> new CoupleNotFoundException("일치하는 회원이 존재하지 않습니다."));
    }

    /**
     * - 커플 데이터 삭제
     **/
    public void deleteCouple(Couple couple) {
        this.coupleRepository.delete(couple);
    }

}