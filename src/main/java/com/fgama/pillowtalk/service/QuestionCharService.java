package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Question;
import com.fgama.pillowtalk.domain.QuestionChar;
import com.fgama.pillowtalk.repository.CoupleRepository;
import com.fgama.pillowtalk.repository.QuestionCharRepository;
import com.fgama.pillowtalk.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class QuestionCharService {

    private final QuestionCharRepository questionCharRepository;

    public Long join(QuestionChar questionChar){
        QuestionChar save = questionCharRepository.save(questionChar);
        return save.getId();
    }

    public List<QuestionChar> findByQuestionId(Long id){
        List<QuestionChar> questions = questionCharRepository.findByQuestionId(id);
        return questions;
    }

    public List<Long> getHighlight(Long id){
        List<QuestionChar> questions = questionCharRepository.findByQuestionId(id);

        List<Long> highlight = new ArrayList<>();
        for (QuestionChar questionChar : questions) {
            if (Objects.equals(questionChar.getColor(), "purple"))
                highlight.add(questionChar.getNumber());
        }
        return highlight;
    }
}
