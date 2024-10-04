package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.dto.UserReadDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.example.chatservermessage.global.constant.Constants.REDIS_CHAT_PREFIX;
import static com.example.chatservermessage.global.constant.Constants.REDIS_PARTICIPATED_KEY;

@Slf4j(topic = "ChatParticipantsService")
@Service
@RequiredArgsConstructor
public class RedisParticipantsService {

    private final RedisTemplate<String, Boolean> participatedTemplate;
    private final RedisTemplate<String, String> pubSubTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void updateParticipants(String event, String chatId, String email) throws JsonProcessingException {
        if (event.equals("PUT")) {
            participatedTemplate.opsForHash()
                    .put(REDIS_PARTICIPATED_KEY + chatId, email, true);
        }

        if (event.equals("DELETE")) {
            participatedTemplate.delete(REDIS_PARTICIPATED_KEY + chatId);
        }

        Map<Object, Object> entries =
                participatedTemplate.opsForHash().entries(REDIS_PARTICIPATED_KEY + chatId);

        // UserReadDTO 리스트 생성
        List<UserReadDTO> userReadList = entries.entrySet().stream()
                .map(e -> new UserReadDTO((String) e.getKey(), (Boolean) e.getValue()))
                .toList();

        // redis 송신
        String userReadListString = objectMapper.writeValueAsString(userReadList);
        pubSubTemplate.convertAndSend(REDIS_CHAT_PREFIX + chatId, userReadListString);
    }

    public Boolean checkParticipants(String chatId, String email) {
        return participatedTemplate.opsForHash().hasKey(REDIS_PARTICIPATED_KEY + chatId, email);
    }

    public Long getSize(String chatId) {
        return participatedTemplate.opsForHash().size(REDIS_PARTICIPATED_KEY + chatId);
    }
}
