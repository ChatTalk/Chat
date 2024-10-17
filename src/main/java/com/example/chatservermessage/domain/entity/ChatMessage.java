package com.example.chatservermessage.domain.entity;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "type")
    private ChatMessageType type;

    @Column(name = "usernmae")
    private String username;

    @Column(name = "message")
    private String message;

    @Column(name = "created")
    private LocalDateTime createdAt;

    public ChatMessage(ChatMessageDTO dto) {
        this.chatId = dto.getChatId();
        this.type = dto.getType();
        this.username = dto.getUsername();
        this.message = dto.getMessage();
        this.createdAt = dto.getCreatedAt();
    }
}
