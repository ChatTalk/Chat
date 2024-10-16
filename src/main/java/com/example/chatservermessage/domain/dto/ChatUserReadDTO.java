package com.example.chatservermessage.domain.dto;

public record ChatUserReadDTO(String chatId, String email, Boolean read, Boolean leave) {
}
