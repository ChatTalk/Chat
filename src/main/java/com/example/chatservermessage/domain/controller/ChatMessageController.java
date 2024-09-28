package com.example.chatservermessage.domain.controller;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

import static com.example.chatservermessage.global.constant.Constants.*;

@Slf4j(topic = "ChatMessageController")
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final RedisTemplate<String, String> subscribeTemplate;
    private final RedisTemplate<String, String> participatedTemplate;

    // 사용자의 채팅방 입장
    @MessageMapping(value = "/chat/enter")
    public void enter(ChatMessageDTO.Enter enter, Principal principal) {
        log.info("{}번 채팅방에서 클라이언트로부터 {} 회원이 입장 요청",
                enter.getChatId(), principal.getName());

        if (Boolean.TRUE.equals(subscribeTemplate.opsForSet().isMember(REDIS_SUBSCRIBE_KEY + principal.getName(), enter.getChatId()))) {
            log.info("이미 해당 {}번 채팅방 구독 중인 유저 {}:", enter.getChatId(), principal.getName());
            return;
        }

        subscribeTemplate.opsForSet()
                .add(REDIS_SUBSCRIBE_KEY + principal.getName(), enter.getChatId());
        participatedTemplate.opsForList()
                .rightPush(REDIS_PARTICIPATED_KEY + enter.getChatId(), principal.getName());

        ChatMessage message = new ChatMessage(enter, principal.getName());
        ChatMessageDTO dto = new ChatMessageDTO(message);

        messagingTemplate.convertAndSend(CHAT_DESTINATION + dto.getChatId(), dto);
    }

    // 사용자의 메세지 입력 송수신
    @MessageMapping(value = "/chat/message")
    public void message(ChatMessageDTO.Send send, Principal principal) {
        log.info("{}번 채팅방에서 클라이언트로부터 {} 회원이 메세지 전송 요청: {}",
                send.getChatId(), principal.getName(), send.getMessage());

        ChatMessage message = new ChatMessage(send, principal.getName());
        ChatMessageDTO dto = new ChatMessageDTO(message);

        messagingTemplate.convertAndSend(CHAT_DESTINATION + dto.getChatId(), dto);
    }

    // 사용자의 채팅방 퇴장
    @MessageMapping(value = "/chat/leave")
    public void leave(ChatMessageDTO.Leave leave, Principal principal) {
        log.info("{}번 채팅방에서 클라이언트로부터 {} 회원이 퇴장 요청",
                leave.getChatId(), principal.getName());

        subscribeTemplate.opsForSet()
                .remove(REDIS_SUBSCRIBE_KEY + principal.getName(), leave.getChatId());
        participatedTemplate.opsForList()
                .remove(REDIS_PARTICIPATED_KEY + leave.getChatId(), 0, principal.getName());

        ChatMessage message = new ChatMessage(leave, principal.getName());
        ChatMessageDTO dto = new ChatMessageDTO(message);

        messagingTemplate.convertAndSend(CHAT_DESTINATION + dto.getChatId(), dto);
    }
}
