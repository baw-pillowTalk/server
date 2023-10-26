package com.fgama.pillowtalk.repository;

import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {


//    @Query("select m.number from Question m order by m.number limit 1")
//    int findLastIndex();


    int findLastByOrderByIdDesc();
    Question findQuestionByTitle(String title);
}

