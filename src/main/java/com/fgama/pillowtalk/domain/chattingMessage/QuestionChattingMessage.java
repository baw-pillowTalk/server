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
@DiscriminatorValue("QUESTION")
@Getter
public class QuestionChattingMessage extends ChattingMessage{
    private Long questionIndex;

    @Builder
    public QuestionChattingMessage(Member self, boolean isRead, ChattingRoom chattingRoom, Long number, Long questionIndex) {
        super(self, isRead, chattingRoom,number);
        this.questionIndex = questionIndex;
    }

    @Override
    public String getMessageType() {
        return "questionChatting";
    }

    @Override
    public JSONObject getJSONObject(Member member) {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("coupleQuestion", this.questionIndex);

        jsonMessage.put("index", this.getNumber());
        jsonMessage.put("type", this.getMessageType());
        jsonMessage.put("createAt", this.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonMessage.put("chatSender", this.getMember().getNickname());
        jsonMessage.put("mine", this.getMember().getNickname().equals(member.getNickname()));
        jsonMessage.put("isRead", this.getIsRead());
        return jsonMessage;
    }
}
