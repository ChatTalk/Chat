package com.example.chatservermessage.domain.controller;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.service.ChatMessageService;
import com.example.chatservermessage.global.redis.RedisSubscribeService;
import com.example.chatservermessage.global.user.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;

import static com.example.chatservermessage.global.constant.Constants.REDIS_CHAT_PREFIX;

@Slf4j(topic = "ChatMessageController")
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final RedisSubscribeService redisSubscribeService;

    // 사용자의 채팅방 입장
    @MessageMapping(value = "/chat/enter")
    public void enter(ChatMessageDTO.Enter enter, @AuthenticationPrincipal UserDetailsImpl userDetails) throws JsonProcessingException {
        log.info("{}번 채팅방에서 클라이언트로부터 {} 회원이 입장 요청",
                enter.getChatId(), userDetails.getUsername());

        redisSubscribeService.subscribe(REDIS_CHAT_PREFIX + enter.getChatId());
        chatMessageService.enter(enter, userDetails);
    }

    // 사용자의 메세지 입력 송수신
    @MessageMapping(value = "/chat/message")
    public void message(ChatMessageDTO.Send send, Principal principal) {
        log.info("{}번 채팅방에서 클라이언트로부터 {} 회원이 메세지 전송 요청: {}",
                send.getChatId(), principal.getName(), send.getMessage());

        chatMessageService.message(send, principal);
    }

    // 사용자의 채팅방 퇴장
    @MessageMapping(value = "/chat/leave")
    public void leave(ChatMessageDTO.Leave leave, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("{}번 채팅방에서 클라이언트로부터 {} 회원이 퇴장 요청",
                leave.getChatId(), userDetails.getUsername());

        redisSubscribeService.unsubscribe(REDIS_CHAT_PREFIX + leave.getChatId());
        chatMessageService.leave(leave, userDetails);
    }
}
