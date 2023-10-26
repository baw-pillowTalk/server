package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.config.Constants;
import com.fgama.pillowtalk.domain.NoticeBoard;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.service.NoticeBoardService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class NoticeBoardApi {
    private final NoticeBoardService noticeBoardService;

    @GetMapping("api/v1/notice-boards")
    public JSendResponse getNoticeList(@RequestBody NoticeBoardListRequest request) {
        try {
            List<NoticeBoard> noticeBoardList = noticeBoardService.getNoticeBoardList(request.getPageNo());

            JSONArray jsonArray = new JSONArray();
            for (int i = noticeBoardList.size() - 1; i >= 0; i--) {
                NoticeBoard noticeBoard = noticeBoardList.get(i);
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("id", noticeBoard.getId());
                jsonMessage.put("title", noticeBoard.getTitle());
                jsonMessage.put("body", noticeBoard.getBody());
                jsonArray.add(jsonMessage);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("NoticeBoardList", jsonArray);

            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    @GetMapping("api/v1/notice-board")
    public JSendResponse getNotice(@RequestBody NoticeBoardRequest request) {
        try {
            NoticeBoard noticeBoardList = noticeBoardService.getNoticeBoard(request.getIndex());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("body", noticeBoardList.getBody());

            return new JSendResponse(Constants.HTTP_SUCCESS, null, jsonObject);
        } catch (Exception e) {
            return new JSendResponse(Constants.HTTP_FAIL, e.toString());
        }
    }

    @PostMapping("api/v1/board")
    public void add(@RequestBody addBoardRequest request) {
        NoticeBoard noticeBoard = new NoticeBoard();
        noticeBoard.setTitle(request.getTitle());
        noticeBoard.setBody(request.getBody());
        noticeBoard.setCreateAt(LocalDateTime.now());
        noticeBoard.setCreator("master");
        noticeBoardService.join(noticeBoard);
    }

    @Data
    static class NoticeBoardListRequest {
        private int pageNo;

    }

    @Data
    static class NoticeBoardRequest {
        private Long index;

    }

    @Data
    static class addBoardRequest {
        private String title;
        private String body;

    }

}