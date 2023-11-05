package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.CoupleChallenge;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeJsonService {

    public JSONObject getChallengeData(CoupleChallenge coupleChallenge) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("index", coupleChallenge.getNumber());
        jsonObject.put("pageNo", coupleChallenge.getNumber() / 4);
        jsonObject.put("title", coupleChallenge.getTitle());
        jsonObject.put("deadline", coupleChallenge.getTargetDate());
        jsonObject.put("content", coupleChallenge.getBody());
        jsonObject.put("creator", coupleChallenge.getCreator());
        jsonObject.put("isCompleted", coupleChallenge.getDone());

        return jsonObject;
    }
}
