package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.domain.Question;
import com.fgama.pillowtalk.service.ChattingRoomService;
import com.fgama.pillowtalk.service.MemberService;
import com.fgama.pillowtalk.service.QuestionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

@RestController
@RequiredArgsConstructor
public class QuestionApi {
    private final QuestionService questionService;
    private final MemberService memberService;
    private final ChattingRoomService chattingRoomService;

    /***
     * 질문 추가
     * 기획에서 정해준 질문 파일로 만들어서 디비에 저장하는 코드
     * @return
     */
    @PostMapping("/api/questions")
    public String AddQuestions() {
        try {
            //파일 객체 생성
            File file = new File("/Users/choejong-geun/Spring/pillowtalk/src/main/resources/static/questionList.txt");
            //입력 스트림 생성
            FileReader filereader = new FileReader(file);
            //입력 버퍼 생성
            BufferedReader bufReader = new BufferedReader(filereader);
            String line = "";
            int size = 0;
            while ((line = bufReader.readLine()) != null) {
                String line2 = bufReader.readLine();
                Question question = Question.builder().number(size++).header(line).body(line2).build();
                questionService.join(question);
            }
            //.readLine()은 끝에 개행문자를 읽지 않는다.
            bufReader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "기존 질문 추가 성공";
    }

    @PostMapping("/api/question/detail")
    public QuestionResponse getHeader(@RequestBody QuestionRequest request) {
        Question question = questionService.findQuestionByTitle(request.getTitle());
        return new QuestionResponse(question.getHeader(), question.getBody());
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class QuestionRequest {
        private String header;
        private String body;
        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class QuestionResponse {
        private String header;
        private String body;
    }

}