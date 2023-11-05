package com.fgama.pillowtalk.dto;

import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONObject;


@Setter
@Getter
public class JSendResponse {
    private String status;
    private String message;
    private JSONObject data;

    public JSendResponse(String status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public JSendResponse(String status, String message, JSONObject data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}