package com.example.chatservermessage.domain.controller;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.security.Principal;

import static com.example.chatservermessage.global.constant.Constants.CHAT_DESTINATION;

@Slf4j
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final SimpMessageSendingOperations messagingTemplate;

    // 사용자의 채팅방 입장
    @MessageMapping(value = "/chat/enter")
    public void enter(ChatMessageDTO.Enter enter, Principal principal) {
        log.info("{}번 채팅방에서 클라이언트로부터 {} 회원이 입장 요청",
                enter.getChatId(), principal.getName());

        ChatMessage message = new ChatMessage(enter, principal.getName());
        ChatMessageDTO dto = new ChatMessageDTO(message);

        messagingTemplate.convertAndSend(
                CHAT_DESTINATION + dto.getChatId(), dto);
    }

    // 사용자의 메세지 입력 송수신
    @MessageMapping(value = "/chat/message")
    public void message(ChatMessageDTO.Send send, Principal principal) {
        log.info("{}반 채팅방에서 클라이언트로부터 {} 회원이 메세지 전송 요청: {}",
                send.getChatId(), principal.getName(), send.getMessage());

        ChatMessage message = new ChatMessage(send, principal.getName());
        ChatMessageDTO dto = new ChatMessageDTO(message);

        messagingTemplate.convertAndSend(
                CHAT_DESTINATION + dto.getChatId(), dto);
    }

    // 사용자의 채팅방 퇴장
    @MessageMapping(value = "/chat/leave")
    public void leave(ChatMessageDTO.Leave leave, Principal principal) {
        log.info("{}반 채팅방에서 클라이언트로부터 {} 회원이 퇴장 요청",
                leave.getChatId(), principal.getName());

        // 채팅 메세지 엔티티 관련 서비스 로직(mongoDB) 수행
        ChatMessage message = new ChatMessage(leave, principal.getName());
        ChatMessageDTO dto = new ChatMessageDTO(message);

        messagingTemplate.convertAndSend(
                CHAT_DESTINATION + dto.getChatId(), dto);
    }
}
