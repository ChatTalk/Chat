package com.example.chatservermessage.domain.controller;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.service.ChatReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class ChatReadController {

    private final ChatReadService chatReadService;

    @GetMapping("/{chatId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(
            @PathVariable Long chatId, @AuthenticationPrincipal UserDetails userDetails) {
        List<ChatMessageDTO> unreadMessages =
                chatReadService.getUnreadMessages(userDetails.getUsername(), chatId.toString());

        return ResponseEntity.ok(unreadMessages);
    }
}
