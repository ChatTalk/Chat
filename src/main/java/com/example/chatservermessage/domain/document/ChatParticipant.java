package com.example.chatservermessage.domain.document;

import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Getter
@Document(collection = "participants")
public class ChatParticipant {

    @Id
    private String id; // MongoDB 기본 키

    private Map<String, Boolean> participant;

    /**
     * 생성자
     * @param email
     * 처음 생성 당시에는 구독하면서 읽을 테니 true 처리
     */
    public ChatParticipant(String email) {
        this.participant.put(email, true);
    }

    /**
     * @param email
     * 접속만 끊기
     */
    public void nonParticipant(String email) {
        this.participant.put(email, false);
    }

    /**
     * @param email
     * 채팅방 퇴장
     */
    public void exit(String email) {
        this.participant.remove(email);
    }
}
