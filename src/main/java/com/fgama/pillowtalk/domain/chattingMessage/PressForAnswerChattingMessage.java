package com.fgama.pillowtalk.domain.chattingMessage;

import com.fgama.pillowtalk.domain.ChattingRoom;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.format.DateTimeFormatter;

@Entity
@NoArgsConstructor
@DiscriminatorValue("PRESS_FOR_ANSWER")
@Getter
public class PressForAnswerChattingMessage extends ChattingMessage{
    private Long coupleQuestionIndex;


    @Builder
    public PressForAnswerChattingMessage(Member self, boolean isRead, ChattingRoom chattingRoom, Long number, Long coupleQuestionIndex) {
        super(self, isRead, chattingRoom,number);
        this.coupleQuestionIndex = coupleQuestionIndex;
    }

    @Override
    public String getMessageType() {
        return "pressForAnswerChatting";
    }

    @Override
    public JSONObject getJSONObject(Member member) {
        JSONObject jsonMessage = new JSONObject();

        jsonMessage.put("coupleQuestion", this.coupleQuestionIndex);
        jsonMessage.put("index", this.getNumber());
        jsonMessage.put("type", this.getMessageType());
        jsonMessage.put("createAt", this.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonMessage.put("chatSender", this.getMember().getNickname());
        jsonMessage.put("mine", this.getMember().getNickname().equals(member.getNickname()));
        jsonMessage.put("isRead", this.getIsRead());
        return jsonMessage;
    }
}
