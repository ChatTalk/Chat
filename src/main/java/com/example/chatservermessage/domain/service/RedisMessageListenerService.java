package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.dto.UserReadDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisMessageListenerService implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());

        log.info("채널 확인: {}", channel);
        log.info("날 것 그대로의 메세지: " + body);

        try {
            log.info("채널 확인: {}", channel);
            log.info("날 것 그대로의 메세지: " + body);

            List<UserReadDTO> dto = new ObjectMapper().readValue(body, new TypeReference<List<UserReadDTO>>() {});
            log.info("파싱: {}", dto.toString());
//            log.info("값 확인: " + dto.toString());

            // 메시지를 모든 WebSocket 클라이언트에게 브로드캐스트
//            messagingTemplate.convertAndSend(
//                    CHAT_DESTINATION + channel, dto);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
