package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Question;
import com.fgama.pillowtalk.repository.CoupleRepository;
import com.fgama.pillowtalk.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CoupleRepository coupleRepository;

    public Long join(Question question){
        Question save = questionRepository.save(question);
        return save.getId();
    }

    public List<Question> findAll(){
        return questionRepository.findAll();

    }

    public void getHighlight(){

    }


    public Question findQuestionByTitle(String title){
        return questionRepository.findQuestionByTitle(title);
    }


    public int getLastIndex(){
        return questionRepository.findLastByOrderByIdDesc();
    }
}
