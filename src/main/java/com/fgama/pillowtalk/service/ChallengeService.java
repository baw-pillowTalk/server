package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Challenge;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.CoupleQuestion;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.exceptions.MemberNotFoundException;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.repository.ChallengeRepository;
import com.fgama.pillowtalk.repository.CoupleRepository;
import com.fgama.pillowtalk.repository.MemberRepository;
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

    private final MemberRepository memberRepository;
    private final CoupleRepository coupleRepository;

    private final ChallengeRepository challengeRepository;

    public Long join(Challenge challenge) {
        challengeRepository.save(challenge);
        return challenge.getId();
    }

    public List<Challenge> findAll() {
        return challengeRepository.findAll();
    }

    public Challenge getChallenge(String accessToken, Long id) throws NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);

        return challengeRepository.findByCoupleIdAndNumber(member.getCoupleId(), id);
    }

    public Challenge addChallenge(String accessToken,  String title, String body, String targetDate) throws NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        Couple coupleById = coupleRepository.findCoupleById(member.getCoupleId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime dateTime = LocalDate.parse(targetDate, formatter).atStartOfDay();
        Challenge challenge = Challenge.builder()
//                .category(category)
                .title(title)
                .body(body)
                .targetDate(dateTime)
                .couple(coupleById)
                .creator(member.getNickname())
                .done(false)
                .number((long) challengeRepository.findByCoupleId(member.getCoupleId()).size())
                .createAt(LocalDateTime.now())
                .build();

        return challengeRepository.save(challenge);
    }

    public Challenge updateChallenge(String accessToken,Long index,String title, String body, String targetDate) throws NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        Couple coupleById = coupleRepository.findCoupleById(member.getCoupleId());
        Challenge challenge1 = challengeRepository.findByCoupleIdAndNumber(coupleById.getId(),index);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime dateTime = LocalDate.parse(targetDate, formatter).atStartOfDay();
//        challenge1.setCategory(category);
        challenge1.setTitle(title);
        challenge1.setBody(body);
        challenge1.setTargetDate(dateTime);

        return challengeRepository.save(challenge1);
    }
    public Long doneChallenge(String accessToken,Long index) throws RuntimeException , NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        Couple coupleById = coupleRepository.findCoupleById(member.getCoupleId());
        Challenge challenge1 = challengeRepository.findByCoupleIdAndNumber(coupleById.getId(),index);
        if (challenge1.getDone() == true){
            throw new RuntimeException("이미 완료된 챌린지입니다.");
        }
        challenge1.setDone(true);


        return challengeRepository.save(challenge1).getId();
    }
    public void deleteChallenge(String accessToken,Long index) throws NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);

        challengeRepository.deleteByCoupleIdAndNumber(member.getCoupleId(),index);
    }

    public void sortNumber(String accessToken){
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        Couple coupleById = coupleRepository.findCoupleById(member.getCoupleId());
        List<Challenge> challenges = challengeRepository.findByCoupleId(coupleById.getId());

        Long index = 0L;
        for (Challenge challenge : challenges){
            challenge.setNumber(index);
            challengeRepository.save(challenge);
            index++;
        }
    }

    public List<Integer> getCount(String accessToken) throws NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        List<Challenge> challenges = challengeRepository.findByCoupleId(member.getCoupleId());
        int count = 0;
        for (Challenge challenge : challenges){
            if (challenge.getDone() == true){
                count+=1;
            }
        }

        List<Integer> result = new ArrayList<>();
        result.add(challenges.size()); // total
        result.add(challenges.size() - count); //ongoing
        result.add(count); //done
        return  result;
    }

    public int getLatestChallengePageNoInProgress(String accessToken) throws RuntimeException {
        Member memberByAccessToken = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        if (memberByAccessToken.getCoupleId() == null){
            throw new RuntimeException("커플없음");
        }
        Sort sort = Sort.by(Sort.Direction.DESC,"createAt");
        PageRequest pageRequest = PageRequest.of(0,4 ,sort);
        Page<Challenge> pageByCoupleId = challengeRepository.findPageByCoupleIdAndDone(memberByAccessToken.getCoupleId(),false, pageRequest);
        if (pageByCoupleId.getTotalPages() == 0){
            return pageByCoupleId.getTotalPages();
        }else {
            return pageByCoupleId.getTotalPages() - 1;
        }
    }
 public int getLatestChallengePageNoDone(String accessToken) throws RuntimeException {
        Member memberByAccessToken = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        if (memberByAccessToken.getCoupleId() == null){
            throw new RuntimeException("커플없음");
        }
        Sort sort = Sort.by(Sort.Direction.ASC,"createAt");
        PageRequest pageRequest = PageRequest.of(0,4 ,sort);
        Page<Challenge> pageByCoupleId = challengeRepository.findPageByCoupleIdAndDone(memberByAccessToken.getCoupleId(),true, pageRequest);
        if (pageByCoupleId.getTotalPages() == 0){
            return pageByCoupleId.getTotalPages();
        }else {
            return pageByCoupleId.getTotalPages() - 1;
        }
    }

    public List<Challenge> getChallengeData(String accessToken) throws NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        return challengeRepository.findByCoupleId(member.getCoupleId());
    }

    public Challenge findByIndex(String accessToken, int index) throws NullPointerException{
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(NullPointerException::new);
        Couple couple = coupleRepository.findById(member.getCoupleId()).orElseThrow(NullPointerException::new);
        return couple.getChallenges().get(index);
    }


    public List<Challenge> getInProgressChallengeList(String accessToken , int pageNo){
        Member memberByAccessToken = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(() -> new MemberNotFoundException());
        Sort sort = Sort.by(Sort.Direction.DESC,"targetDate");
        PageRequest pageRequest = PageRequest.of(pageNo,4 ,sort);
        List<Challenge> challenges = challengeRepository.findByCoupleIdAndDone(memberByAccessToken.getCoupleId() ,false,pageRequest);
        if (challenges == null){
            throw new NullPointerException("커플 챌린지 비었음");
        }
        return challenges;
    }
    public List<Challenge> getDoneChallengeList(String accessToken , int pageNo){
        Member memberByAccessToken = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(() -> new MemberNotFoundException());
        Sort sort = Sort.by(Sort.Direction.ASC,"createAt");
        PageRequest pageRequest = PageRequest.of(pageNo,4 ,sort);
        List<Challenge> challenges = challengeRepository.findByCoupleIdAndDone(memberByAccessToken.getCoupleId() ,true,pageRequest);
        if (challenges == null){
            throw new NullPointerException("커플 챌린지 비었음");
        }
        return challenges;
    }
}

