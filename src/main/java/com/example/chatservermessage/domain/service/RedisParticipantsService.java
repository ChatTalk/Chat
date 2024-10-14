package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.dto.ChatUserReadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.example.chatservermessage.global.constant.Constants.REDIS_CHAT_PREFIX;

@Slf4j(topic = "ChatParticipantsService")
@Service
@RequiredArgsConstructor
public class RedisParticipantsService {

    private final RedisTemplate<String, ChatUserReadDTO> pubSubTemplate;

    public void participate(String chatId, String email) {
        ChatUserReadDTO chatUserReadDTO = new ChatUserReadDTO(chatId, email, true, false);
        pubSubTemplate.convertAndSend(REDIS_CHAT_PREFIX + chatId, chatUserReadDTO);
    }

    public void leave(String chatId, String email) {
        ChatUserReadDTO chatUserReadDTO = new ChatUserReadDTO(chatId, email, false, true);
        pubSubTemplate.convertAndSend(REDIS_CHAT_PREFIX + chatId, chatUserReadDTO);
    }
}
