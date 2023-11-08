package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.CoupleChallenge;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.repository.ChallengeRepository;
import com.fgama.pillowtalk.repository.CoupleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeService {


    private final CoupleRepository coupleRepository;
    private final ChallengeRepository challengeRepository;

    private final CoupleService coupleService;
    private final MemberService memberService;

    public Long join(CoupleChallenge coupleChallenge) {
        challengeRepository.save(coupleChallenge);
        return coupleChallenge.getId();
    }

    public List<CoupleChallenge> findAll() {
        return challengeRepository.findAll();
    }

    public CoupleChallenge getChallenge(String accessToken, int id) throws NullPointerException {
        Member member = this.getMember();
        return challengeRepository.findByCoupleIdAndNumber(member.getCoupleId(), id);
    }

    public CoupleChallenge addChallenge(String accessToken, String title, String body, String targetDate) throws NullPointerException {
        Member member = this.getMember();
        Couple couple = this.coupleService.getCouple(member);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime dateTime = LocalDate.parse(targetDate, formatter).atStartOfDay();
        CoupleChallenge coupleChallenge = CoupleChallenge.builder()
//                .category(category)
                .title(title)
                .body(body)
                .targetDate(dateTime)
                .couple(couple)
                .creator(member.getNickname())
                .done(false)
                .number(challengeRepository.findByCoupleId(member.getCoupleId()).size())
                .build();

        return challengeRepository.save(coupleChallenge);
    }

    public CoupleChallenge updateChallenge(String accessToken, int index, String title, String body, String targetDate) throws NullPointerException {
        Member member = this.getMember();
        Couple couple = this.coupleService.getCouple(member);
        CoupleChallenge coupleChallenge1 = challengeRepository.findByCoupleIdAndNumber(couple.getId(), index);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime dateTime = LocalDate.parse(targetDate, formatter).atStartOfDay();
//        challenge1.setCategory(category);
        coupleChallenge1.setTitle(title);
        coupleChallenge1.setBody(body);
        coupleChallenge1.setTargetDate(dateTime);

        return challengeRepository.save(coupleChallenge1);
    }

    public Long doneChallenge(String accessToken, int index) throws RuntimeException {
        Member member = this.getMember();
        Couple couple = this.coupleService.getCouple(member);
        CoupleChallenge coupleChallenge1 = challengeRepository.findByCoupleIdAndNumber(couple.getId(), index);
        if (coupleChallenge1.getDone()) {
            throw new RuntimeException("이미 완료된 챌린지입니다.");
        }
        coupleChallenge1.setDone(true);


        return challengeRepository.save(coupleChallenge1).getId();
    }

    public void deleteChallenge(String accessToken, int index) throws NullPointerException {
        Member member = this.getMember();

        challengeRepository.deleteByCoupleIdAndNumber(member.getCoupleId(), index);
    }

    public void sortNumber(String accessToken) {
        Member member = this.getMember();
        Couple couple = this.coupleService.getCouple(member);
        List<CoupleChallenge> coupleChallenges = challengeRepository.findByCoupleId(couple.getId());

        int index = 0;
        for (CoupleChallenge coupleChallenge : coupleChallenges) {
            coupleChallenge.setNumber(index);
            challengeRepository.save(coupleChallenge);
            index++;
        }
    }

    public List<Integer> getCount(String accessToken) throws NullPointerException {
        Member member = this.getMember();
        List<CoupleChallenge> coupleChallenges = challengeRepository.findByCoupleId(member.getCoupleId());
        int count = 0;
        for (CoupleChallenge coupleChallenge : coupleChallenges) {
            if (coupleChallenge.getDone()) {
                count += 1;
            }
        }

        List<Integer> result = new ArrayList<>();
        result.add(coupleChallenges.size()); // total
        result.add(coupleChallenges.size() - count); //ongoing
        result.add(count); //done
        return result;
    }

    public int getLatestChallengePageNoInProgress(String accessToken) throws RuntimeException {
        Member member = this.getMember();
        if (member.getCoupleId() == null) {
            throw new RuntimeException("커플없음");
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createAt");
        PageRequest pageRequest = PageRequest.of(0, 4, sort);
        Page<CoupleChallenge> pageByCoupleId = challengeRepository.findPageByCoupleIdAndDone(member.getCoupleId(), false, pageRequest);
        if (pageByCoupleId.getTotalPages() == 0) {
            return pageByCoupleId.getTotalPages();
        } else {
            return pageByCoupleId.getTotalPages() - 1;
        }
    }

    public int getLatestChallengePageNoDone(String accessToken) throws RuntimeException {
        Member member = this.getMember();
        if (member.getCoupleId() == null) {
            throw new RuntimeException("커플없음");
        }
        Sort sort = Sort.by(Sort.Direction.ASC, "createAt");
        PageRequest pageRequest = PageRequest.of(0, 4, sort);
        Page<CoupleChallenge> pageByCoupleId = challengeRepository.findPageByCoupleIdAndDone(member.getCoupleId(), true, pageRequest);
        if (pageByCoupleId.getTotalPages() == 0) {
            return pageByCoupleId.getTotalPages();
        } else {
            return pageByCoupleId.getTotalPages() - 1;
        }
    }

    public List<CoupleChallenge> getChallengeData(String accessToken) throws NullPointerException {
        Member member = this.getMember();
        return challengeRepository.findByCoupleId(member.getCoupleId());
    }

    public CoupleChallenge findByIndex(String accessToken, int index) throws NullPointerException {
        Member member = this.getMember();
        Couple couple = coupleRepository.findById(member.getCoupleId()).orElseThrow(NullPointerException::new);
        return couple.getCoupleChallenges().get(index);
    }


    public List<CoupleChallenge> getInProgressChallengeList(String accessToken, int pageNo) {
        Member member = this.getMember();
        Sort sort = Sort.by(Sort.Direction.DESC, "targetDate");
        PageRequest pageRequest = PageRequest.of(pageNo, 4, sort);
        List<CoupleChallenge> coupleChallenges = challengeRepository.findByCoupleIdAndDone(member.getCoupleId(), false, pageRequest);
        if (coupleChallenges == null) {
            throw new NullPointerException("커플 챌린지 비었음");
        }
        return coupleChallenges;
    }

    public List<CoupleChallenge> getDoneChallengeList(String accessToken, int pageNo) {
        Member member = this.getMember();
        Sort sort = Sort.by(Sort.Direction.ASC, "createAt");
        PageRequest pageRequest = PageRequest.of(pageNo, 4, sort);
        List<CoupleChallenge> coupleChallenges = challengeRepository.findByCoupleIdAndDone(member.getCoupleId(), true, pageRequest);
        if (coupleChallenges == null) {
            throw new NullPointerException("커플 챌린지 비었음");
        }
        return coupleChallenges;
    }

    public Member getMember() {
        return this.memberService.getCurrentMember();
    }
}

