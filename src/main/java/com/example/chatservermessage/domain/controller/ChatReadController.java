package com.example.chatservermessage.domain.controller;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.service.ChatReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.chatservermessage.global.constant.Constants.REDIS_CHAT_READ_KEY;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class ChatReadController {

    private final ChatReadService chatReadService;
    private final RedisTemplate<String, String> readTemplate;

    @GetMapping("/{chatId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(
            @PathVariable Long chatId, @AuthenticationPrincipal UserDetails userDetails) {
        List<ChatMessageDTO> unreadMessages =
                chatReadService.getUnreadMessages(userDetails.getUsername(), chatId.toString());

        return ResponseEntity.ok(unreadMessages);
    }

    @PutMapping()
    public void unread(@AuthenticationPrincipal UserDetails userDetails) {
        readTemplate.delete(REDIS_CHAT_READ_KEY + userDetails.getUsername());
    }
}
