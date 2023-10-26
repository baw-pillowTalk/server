package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.Question;
import com.fgama.pillowtalk.domain.QuestionChar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionCharRepository extends JpaRepository<QuestionChar, Long> {
    List<QuestionChar> findByQuestionId(Long id);

}

