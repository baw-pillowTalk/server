package com.fgama.pillowtalk.api;

import com.fgama.pillowtalk.constant.HttpResponse;
import com.fgama.pillowtalk.dto.JSendResponse;
import com.fgama.pillowtalk.service.PillowTalkBoardService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PillowTalkBoardApi {
    private final PillowTalkBoardService pillowTalkBoardService;

    @PostMapping("api/v1/comment")
    public JSendResponse writeComment(@RequestHeader("Authorization") String authorizationHeader,
                                      @RequestBody WriteCommentRequest request) {
        try {
            String accessToken = authorizationHeader.substring("Bearer ".length());
            pillowTalkBoardService.addComment(accessToken, request.getTitle(), request.getBody(), request.getEmail());

            return new JSendResponse(HttpResponse.HTTP_SUCCESS, null);
        } catch (RuntimeException e) {
            return new JSendResponse(HttpResponse.HTTP_FAIL, e.toString());

        }
    }

    @Data
    static class WriteCommentRequest {
        private String title;
        private String body;
        private String email;
    }

}