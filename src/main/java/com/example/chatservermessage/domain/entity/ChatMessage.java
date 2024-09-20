package com.example.chatservermessage.domain.entity;


import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    private String id; // 도큐먼트의 아이디
    
    private String chatId; // 채팅방 아이

    private ChatMessageType type; // 메세지 타입
    
    private String username; // 메세지 전송인

    private String message; // 메세지
    
    private String createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 송수신 메세지 생성자
    public ChatMessage(ChatMessageDTO.Send dto, String username) {
        this.chatId = dto.getChatId();
        this.type = ChatMessageType.MESSAGE;
        this.username = username;
        this.message = dto.getMessage();
        this.createdAt = LocalDateTime.now().format(FORMATTER);
    }

    // 입장 메세지 생성자
    public ChatMessage(ChatMessageDTO.Enter dto, String username) {
        this.chatId = dto.getChatId();
        this.type = ChatMessageType.ENTER;
        this.username = username;
        this.message = username + " 님이 입장하셨습니다.";
        this.createdAt = LocalDateTime.now().format(FORMATTER);
    }

    // 퇴장 메세지 생성자
    public ChatMessage(ChatMessageDTO.Leave dto, String username) {
        this.chatId = dto.getChatId();
        this.type = ChatMessageType.LEAVE;
        this.username = username;
        this.message = username + " 님이 퇴장하셨습니다.";
        this.createdAt = LocalDateTime.now().format(FORMATTER);
    }
}
