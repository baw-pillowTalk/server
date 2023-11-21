package com.fgama.pillowtalk.domain.chattingMessage;

import com.fgama.pillowtalk.domain.ChattingRoom;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.CoupleChallenge;
import com.fgama.pillowtalk.domain.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;

import java.time.format.DateTimeFormatter;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@NoArgsConstructor
@DiscriminatorValue("CHALLENGE")
@Getter
public class ChallengeChattingMessage extends ChattingMessage{
    private int challengeIndex;

    @Builder
    public ChallengeChattingMessage(Member self, boolean isRead, ChattingRoom chattingRoom, Long number, int challengeIndex) {
        super(self, isRead, chattingRoom,number);
        this.challengeIndex = challengeIndex;
    }

    @Override
    public String getMessageType() {
        return "challengeChatting";
    }

    @Override
    public JSONObject getJSONObject(Member member) {
        JSONObject jsonMessage = new JSONObject();

        jsonMessage.put("coupleChallenge", this.challengeIndex);
        jsonMessage.put("index", this.getNumber());
        jsonMessage.put("type", this.getMessageType());
        jsonMessage.put("createAt", this.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        jsonMessage.put("chatSender", this.getMember().getNickname());
        jsonMessage.put("mine", this.getMember().getNickname().equals(member.getNickname()));
        jsonMessage.put("isRead", this.getIsRead());
        return jsonMessage;
    }
}
