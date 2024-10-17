package com.example.chatservermessage.domain.dto;

import com.example.chatservermessage.domain.entity.ChatMessage;
import com.example.chatservermessage.domain.entity.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {

    private String chatId;
    private ChatMessageType type;
    private String username;
    private String message;
    private LocalDateTime createdAt;

    public ChatMessageDTO(ChatMessageDTO.Send message, String username) {
        this.chatId = message.getChatId();
        this.type = ChatMessageType.MESSAGE;
        this.username = username;
        this.message = message.getMessage();
        this.createdAt = LocalDateTime.now();
    }

    public ChatMessageDTO(ChatMessageDTO.Enter message, String username) {
        this.chatId = message.getChatId();
        this.type = ChatMessageType.ENTER;
        this.username = username;
        this.message = username + " 님이 입장하셨습니다.";
        this.createdAt = LocalDateTime.now();
    }

    public ChatMessageDTO(ChatMessageDTO.Leave dto, String username) {
        this.chatId = dto.getChatId();
        this.type = ChatMessageType.LEAVE;
        this.username = username;
        this.message = username + " 님이 퇴장하셨습니다.";
        this.createdAt = LocalDateTime.now();
    }

    public ChatMessageDTO(ChatMessage chatMessage) {
        this.chatId = chatMessage.getChatId();
        this.type = chatMessage.getType();
        this.username = chatMessage.getUsername();
        this.message = chatMessage.getMessage();
        this.createdAt = chatMessage.getCreatedAt();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Send {
        private String chatId;
        private String message;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Enter {
        private String chatId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Leave {
        private String chatId;
    }
}
