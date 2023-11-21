package com.fgama.pillowtalk.domain.chattingMessage;

import com.fgama.pillowtalk.domain.BaseEntity;
import com.fgama.pillowtalk.domain.ChattingRoom;
import com.fgama.pillowtalk.domain.Couple;
import com.fgama.pillowtalk.domain.Member;
import com.fgama.pillowtalk.service.MemberService;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.JSONObject;

import javax.persistence.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "chatting_message")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "chatting_message_type", discriminatorType = DiscriminatorType.STRING)
public abstract class ChattingMessage extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "chatting_message_id")
    private Long id;

    private Long number;
    private Boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_room_id")
    private ChattingRoom chattingRoom;

    public ChattingMessage(Member self, boolean isRead, ChattingRoom chattingRoom, Long number) {
        this.member = self;
        this.isRead = isRead;
        this.chattingRoom =chattingRoom;
        this.number = number;
    }

    public abstract String getMessageType();
    public abstract JSONObject getJSONObject(Member member);

}
