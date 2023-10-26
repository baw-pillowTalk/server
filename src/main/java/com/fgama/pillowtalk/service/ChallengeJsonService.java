package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Challenge;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeJsonService {

    public JSONObject getChallengeData(Challenge challenge) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("index", challenge.getNumber());
        jsonObject.put("pageNo", challenge.getNumber() / 4);
        jsonObject.put("title", challenge.getTitle());
        jsonObject.put("deadline", challenge.getTargetDate());
        jsonObject.put("content", challenge.getBody());
        jsonObject.put("creator", challenge.getCreator());
        jsonObject.put("isCompleted", challenge.getDone());

        return jsonObject;
    }
}
