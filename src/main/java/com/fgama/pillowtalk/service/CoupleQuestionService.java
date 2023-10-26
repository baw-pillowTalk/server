package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.CoupleQuestion;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.domain.Question;
import com.fgama.pillowtalk.exceptions.MemberNotFoundException;
import com.fgama.pillowtalk.fcm.FirebaseCloudMessageService;
import com.fgama.pillowtalk.repository.CoupleQuestionRepository;
import com.fgama.pillowtalk.repository.CoupleRepository;
import com.fgama.pillowtalk.repository.MemberRepository;
import com.fgama.pillowtalk.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoupleQuestionService {

    private final CoupleQuestionRepository coupleQuestionRepository;
    private final MemberRepository memberRepository;
    private final CoupleRepository coupleRepository;
    private final QuestionRepository questionRepository;

    private final FirebaseCloudMessageService firebaseCloudMessageService;

    public Long join(CoupleQuestion coupleQuestion) {
        coupleQuestionRepository.save(coupleQuestion);
        return coupleQuestion.getId();
    }

    public List<CoupleQuestion> findAll() {
        return coupleQuestionRepository.findAll();
    }

    public int getLatestQuestionPageNo(String accessToken) {
        Member memberByAccessToken = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        Couple couple = coupleRepository.findById(memberByAccessToken.getCoupleId()).orElseThrow(NullPointerException::new);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, 4, sort);
        Page<CoupleQuestion> byCoupleId = coupleQuestionRepository.findPageByCoupleId(couple.getId(), pageRequest);
        if (byCoupleId.getTotalPages() == 0) {
            return byCoupleId.getTotalPages();
        } else {
            return byCoupleId.getTotalPages() - 1;
        }
    }

    public CoupleQuestion getRecent(String accessToken) throws NullPointerException {
        //다읽어오면 안됨
        Member memberByAccessToken = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(() -> new MemberNotFoundException());
        List<CoupleQuestion> coupleQuestions = coupleQuestionRepository.findByCoupleId(memberByAccessToken.getCoupleId());
        if (coupleQuestions == null) {
            throw new NullPointerException("커플 질문이 비었음");
        }
        return coupleQuestions.get(coupleQuestions.size() - 1);
    }

    public CoupleQuestion getCoupleQuestion(String accessToken, int id) throws NullPointerException {
        //다읽어오면 안됨
        Member memberByAccessToken = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(() -> new MemberNotFoundException());
        List<CoupleQuestion> coupleQuestions = coupleQuestionRepository.findByCoupleId(memberByAccessToken.getCoupleId());
        if (coupleQuestions == null) {
            throw new NullPointerException("커플 질문이 비었음");
        }
        return coupleQuestions.get(id);
    }

    public List<CoupleQuestion> getRecentList(String accessToken, int pageNo) {
        Member memberByAccessToken = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(() -> new MemberNotFoundException());
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageNo, 4, sort);
        List<CoupleQuestion> coupleQuestions = coupleQuestionRepository.findByCoupleId(memberByAccessToken.getCoupleId(), pageRequest);
        if (coupleQuestions == null) {
            throw new NullPointerException("커플 질문이 비었음");
        }
        return coupleQuestions;
    }

    public List<CoupleQuestion> getRecentListDESC(String accessToken, int pageNo) {
        Member memberByAccessToken = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(() -> new MemberNotFoundException());
        List<CoupleQuestion> byCoupleId = coupleQuestionRepository.findByCoupleId(memberByAccessToken.getCoupleId());
        int whole = byCoupleId.size() / 4;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(whole - pageNo, 4, sort);
        List<CoupleQuestion> coupleQuestions = coupleQuestionRepository.findByCoupleId(memberByAccessToken.getCoupleId(), pageRequest);
        if (coupleQuestions == null) {
            throw new NullPointerException("커플 질문이 비었음");
        }
        return coupleQuestions;
    }

    public CoupleQuestion findByIndex(String accessToken, int index) throws NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(NullPointerException::new);
        Couple couple = coupleRepository.findById(member.getCoupleId()).orElseThrow(NullPointerException::new);
        List<CoupleQuestion> questionList = coupleQuestionRepository.findByCoupleId(couple.getId());

        return questionList.get(index);
    }


    public CoupleQuestion updateSelfAnswer(String accessToken, int index, String answer) throws NullPointerException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(MemberNotFoundException::new);
        Couple couple = coupleRepository.findCoupleById(member.getCoupleId());
        CoupleQuestion question = coupleQuestionRepository.findByNumberAndCoupleId(index, couple.getId());
        log.info("updateSelfAnswer" + accessToken + index + answer);

        if (couple.getSelf() == member) {
            question.setSelfAnswer(answer);
            log.info("updateSelfAnswer self변경");
        } else if (couple.getPartner() == member) {
            question.setPartnerAnswer(answer);
            log.info("updateSelfAnswer partner변경");
        }

        return coupleQuestionRepository.save(question);
    }

    public void createCoupleCode() {
        List<Couple> coupleAll = coupleRepository.findAll();
        log.info("시작");
        for (int i = 0; i < coupleAll.size(); i++) {
            log.info("포문");
            Couple couple = coupleAll.get(i);
            if (couple.getCoupleCode() != null) {
                continue;
            }


            List<CoupleQuestion> coupleQuestions = coupleQuestionRepository.findByCoupleId(couple.getId());

            List<Question> all = questionRepository.findAll();
            log.info(String.valueOf(all.size()));

            if (coupleQuestions.isEmpty()) {
                CoupleQuestion coupleQuestion = new CoupleQuestion();
                coupleQuestion.setSelfAnswer(null);
                coupleQuestion.setPartnerAnswer(null);
                coupleQuestion.setCreatedAt(LocalDateTime.now());
                coupleQuestion.setCouple(couple);
                coupleQuestion.setQuestion(all.get(0));
                coupleQuestion.setNumber(coupleQuestions.size());

                coupleQuestionRepository.save(coupleQuestion);


                String fcmDetail = firebaseCloudMessageService.getQuestionFcmJsonObject(
                        "자기야 내 취향은 있잖아\uD83D\uDC93",
                        "newQuestion",
                        "질문에 답변을 남기고 서로를 알아가보세요!",
                        all.get(0).getTitle());

                String partnerFcmToken = couple.getPartner().getFcmToken();
                firebaseCloudMessageService.sendFcmMessage(fcmDetail, partnerFcmToken);

                String selfFcmToken = couple.getSelf().getFcmToken();
                firebaseCloudMessageService.sendFcmMessage(fcmDetail, selfFcmToken);


            } else {
                if (all.size() == coupleQuestions.size()) {
                    log.info("질문없음");
                    continue;
                }


                //질문 없으면 질문안옴
                boolean hasUnansweredQuestion = coupleQuestions.stream()
                        .anyMatch(findCoupleQuestion -> findCoupleQuestion.getSelfAnswer() == null || findCoupleQuestion.getPartnerAnswer() == null);

                if (hasUnansweredQuestion) {
                    continue;
                }


                for (Question findQuestion : all) {
                    boolean check = false;
                    for (CoupleQuestion coupleQuestion : coupleQuestions) {
                        if (findQuestion.getTitle().equals(coupleQuestion.getQuestion().getTitle())) {
                            check = true;
                            break;
                        }


                    }

                    if (!check) {
                        String partnerFcmToken = couple.getPartner().getFcmToken();
                        log.info("질문추가 ");
                        CoupleQuestion coupleQuestion = new CoupleQuestion();
                        coupleQuestion.setSelfAnswer(null);
                        coupleQuestion.setPartnerAnswer(null);
                        coupleQuestion.setCreatedAt(LocalDateTime.now());
                        coupleQuestion.setCouple(couple);
                        coupleQuestion.setQuestion(findQuestion);
                        coupleQuestion.setNumber(coupleQuestions.size());

                        coupleQuestionRepository.save(coupleQuestion);

                        String fcmDetail = firebaseCloudMessageService.getQuestionFcmJsonObject(
                                "자기야 내 취향은 있잖아\uD83D\uDC93",
                                "newQuestion",
                                "질문에 답변을 남기고 서로를 알아가보세요!",
                                findQuestion.getTitle());

                        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partnerFcmToken);

                        String selfFcmToken = couple.getSelf().getFcmToken();
                        firebaseCloudMessageService.sendFcmMessage(fcmDetail, selfFcmToken);

                        log.info("질문추가");
                        break;
                    }
                }
            }
        }
    }

    public void addCoupleQuestion(Long coupleId) {
        List<Couple> coupleAll = coupleRepository.findAll();
        log.info("시작");
        for (int i = 0; i < 1; i++) {
            log.info("포문");
            Couple couple = coupleRepository.findById(coupleId).orElseThrow(NullPointerException::new);
            if (couple.getCoupleCode() != null) {
                continue;
            }


            List<CoupleQuestion> coupleQuestions = coupleQuestionRepository.findByCoupleId(couple.getId());

            List<Question> all = questionRepository.findAll();
            log.info(String.valueOf(all.size()));

            if (coupleQuestions.isEmpty()) {
                CoupleQuestion coupleQuestion = new CoupleQuestion();
                coupleQuestion.setSelfAnswer(null);
                coupleQuestion.setPartnerAnswer(null);
                coupleQuestion.setCreatedAt(LocalDateTime.now());
                coupleQuestion.setCouple(couple);
                coupleQuestion.setQuestion(all.get(0));
                coupleQuestion.setNumber(coupleQuestions.size());

                coupleQuestionRepository.save(coupleQuestion);


                String fcmDetail = firebaseCloudMessageService.getCoupleQuestionFcmJsonObject(
                        "자기야 내 취향은 있잖아\uD83D\uDC93",
                        "todayQuestion",
                        "질문에 답변을 남기고 서로를 알아가보세요!",
                        all.get(0).getNumber());

                String partnerFcmToken = couple.getPartner().getFcmToken();
                firebaseCloudMessageService.sendFcmMessage(fcmDetail, partnerFcmToken);

                String selfFcmToken = couple.getSelf().getFcmToken();
                firebaseCloudMessageService.sendFcmMessage(fcmDetail, selfFcmToken);


            } else {
                if (all.size() == coupleQuestions.size()) {
                    log.info("질문없음");
                    continue;
                }


                //질문 없으면 질문안옴
                boolean hasUnansweredQuestion = coupleQuestions.stream()
                        .anyMatch(findCoupleQuestion -> findCoupleQuestion.getSelfAnswer() == null || findCoupleQuestion.getPartnerAnswer() == null);

                if (hasUnansweredQuestion) {
                    continue;
                }


                for (Question findQuestion : all) {
                    boolean check = false;
                    for (CoupleQuestion coupleQuestion : coupleQuestions) {
                        if (findQuestion.getTitle().equals(coupleQuestion.getQuestion().getTitle())) {
                            check = true;
                            break;
                        }


                    }

                    if (!check) {
                        String partnerFcmToken = couple.getPartner().getFcmToken();
                        log.info("질문추가 ");
                        CoupleQuestion coupleQuestion = new CoupleQuestion();
                        coupleQuestion.setSelfAnswer(null);
                        coupleQuestion.setPartnerAnswer(null);
                        coupleQuestion.setCreatedAt(LocalDateTime.now());
                        coupleQuestion.setCouple(couple);
                        coupleQuestion.setQuestion(findQuestion);
                        coupleQuestion.setNumber(coupleQuestions.size());

                        coupleQuestionRepository.save(coupleQuestion);

                        String fcmDetail = firebaseCloudMessageService.getCoupleQuestionFcmJsonObject(
                                "자기야 내 취향은 있잖아\uD83D\uDC93",
                                "todayQuestion",
                                "질문에 답변을 남기고 서로를 알아가보세요!",
                                findQuestion.getNumber());

                        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partnerFcmToken);

                        String selfFcmToken = couple.getSelf().getFcmToken();
                        firebaseCloudMessageService.sendFcmMessage(fcmDetail, selfFcmToken);

                        log.info("질문추가");
                        break;
                    }
                }
            }
        }
    }

    public void createCoupleCode3() {
        List<Couple> coupleAll = coupleRepository.findAll();
        log.info("시작");
        for (int i = 0; i < 1; i++) {
            log.info("포문");
            Couple couple = coupleRepository.findById(7219L).orElseThrow(NullPointerException::new);
            if (couple.getCoupleCode() != null) {
                continue;
            }


            List<CoupleQuestion> coupleQuestions = coupleQuestionRepository.findByCoupleId(couple.getId());

            List<Question> all = questionRepository.findAll();
            log.info(String.valueOf(all.size()));

            if (coupleQuestions.isEmpty()) {
                CoupleQuestion coupleQuestion = new CoupleQuestion();
                coupleQuestion.setSelfAnswer(null);
                coupleQuestion.setPartnerAnswer(null);
                coupleQuestion.setCreatedAt(LocalDateTime.now());
                coupleQuestion.setCouple(couple);
                coupleQuestion.setQuestion(all.get(0));
                coupleQuestion.setNumber(coupleQuestions.size());

                coupleQuestionRepository.save(coupleQuestion);


                String fcmDetail = firebaseCloudMessageService.getCoupleQuestionFcmJsonObject(
                        "자기야 내 취향은 있잖아\uD83D\uDC93",
                        "todayQuestion",
                        "질문에 답변을 남기고 서로를 알아가보세요!",
                        all.get(0).getNumber());

                String partnerFcmToken = couple.getPartner().getFcmToken();
                firebaseCloudMessageService.sendFcmMessage(fcmDetail, partnerFcmToken);

                String selfFcmToken = couple.getSelf().getFcmToken();
                firebaseCloudMessageService.sendFcmMessage(fcmDetail, selfFcmToken);


            } else {
                if (all.size() == coupleQuestions.size()) {
                    log.info("질문없음");
                    continue;
                }


                //질문 없으면 질문안옴
                boolean hasUnansweredQuestion = coupleQuestions.stream()
                        .anyMatch(findCoupleQuestion -> findCoupleQuestion.getSelfAnswer() == null || findCoupleQuestion.getPartnerAnswer() == null);

                if (hasUnansweredQuestion) {
                    continue;
                }


                for (Question findQuestion : all) {
                    boolean check = false;
                    for (CoupleQuestion coupleQuestion : coupleQuestions) {
                        if (findQuestion.getTitle().equals(coupleQuestion.getQuestion().getTitle())) {
                            check = true;
                            break;
                        }


                    }

                    if (!check) {
                        String partnerFcmToken = couple.getPartner().getFcmToken();
                        log.info("질문추가 ");
                        CoupleQuestion coupleQuestion = new CoupleQuestion();
                        coupleQuestion.setSelfAnswer(null);
                        coupleQuestion.setPartnerAnswer(null);
                        coupleQuestion.setCreatedAt(LocalDateTime.now());
                        coupleQuestion.setCouple(couple);
                        coupleQuestion.setQuestion(findQuestion);
                        coupleQuestion.setNumber(coupleQuestions.size());

                        coupleQuestionRepository.save(coupleQuestion);

                        String fcmDetail = firebaseCloudMessageService.getCoupleQuestionFcmJsonObject(
                                "자기야 내 취향은 있잖아\uD83D\uDC93",
                                "todayQuestion",
                                "질문에 답변을 남기고 서로를 알아가보세요!",
                                findQuestion.getNumber());

                        firebaseCloudMessageService.sendFcmMessage(fcmDetail, partnerFcmToken);

                        String selfFcmToken = couple.getSelf().getFcmToken();
                        firebaseCloudMessageService.sendFcmMessage(fcmDetail, selfFcmToken);

                        log.info("질문추가");
                        break;
                    }
                }
            }
        }
    }

    public int getTotalCount(String accessToken) throws RuntimeException {
        Member member = memberRepository.findMemberByAccessToken(accessToken).orElseThrow(NullPointerException::new);
        Couple couple = coupleRepository.findById(member.getCoupleId()).orElseThrow(NullPointerException::new);
        List<CoupleQuestion> questionList = coupleQuestionRepository.findByCoupleId(couple.getId());
        return questionList.size();
    }
}