package com.example.chatservermessage.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatSubscriptionDTO {

    private String chatId; // 구독한 채팅방 ID
    private List<ChatMessageDTO> unreadMessages;
}
