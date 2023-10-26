package com.fgama.pillowtalk.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Builder
@AllArgsConstructor
@Getter
public class FcmMessage {

    private String title;
    private String title_detail;
    private String type;
    private String message;
    private String message_detail;
    private String createAt;
    private String from_accessToken;



}