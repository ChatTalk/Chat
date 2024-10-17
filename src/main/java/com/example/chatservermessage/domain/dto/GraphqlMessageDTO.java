package com.example.chatservermessage.domain.dto;

import com.example.chatservermessage.domain.entity.ChatMessageType;

public record GraphqlMessageDTO(String chatId, ChatMessageType type, String username, String message, String createdAt) {
}
