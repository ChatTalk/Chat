package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.dto.ChatRoomDTO;
import com.example.chatservermessage.domain.entity.ChatMessage;
import com.example.chatservermessage.domain.repository.ChatMessageRepository;
import com.example.chatservermessage.global.kafka.KafkaMessageService;
import com.example.chatservermessage.global.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j(topic = "ChatMessageService")
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final RedisParticipantsService redisParticipantsService;
    private final KafkaMessageService kafkaMessageService;
    private final GraphqlService graphqlService;
    private final ChatUserSubscriptionService chatUserSubscriptionService;

    private final ChatMessageRepository chatMessageRepository;

    public void enter(ChatMessageDTO.Enter enter, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String username = userDetails.getUsername();
        String role = userDetails.getRole();

        log.info("사용자 이메일: {}", username);
        log.info("사용자 권한: {}", role);

        ChatRoomDTO chatRoomDTO = graphqlService.getChatRoomById(enter.getChatId(), username, role);

        if (chatUserSubscriptionService.isSubscribed(enter.getChatId(), username)) {

            // 여기서 읽지 않은 메세지 조회 로직 트리깅 하면 될 듯?

            log.info("이미 해당 {}번 채팅방 구독 중인 유저 {}:", enter.getChatId(), username);
            redisParticipantsService.participate(enter.getChatId(), username);
            return;
        }

        graphqlService.incrementPersonnel(enter.getChatId(), username, role);
        chatUserSubscriptionService.subscribe(enter.getChatId(), username); // 구독 메타데이터 생성(대기열_
        redisParticipantsService.participate(enter.getChatId(), username); // 접속 데이터 처리(NoSQL)

        ChatMessageDTO dto = new ChatMessageDTO(enter, username);
        ChatMessage message = new ChatMessage(dto);
        chatMessageRepository.save(message);
        kafkaMessageService.send(dto);
    }

    public void message(ChatMessageDTO.Send send, Principal principal) {
        ChatMessageDTO dto = new ChatMessageDTO(send, principal.getName());
        ChatMessage message = new ChatMessage(dto);
        chatMessageRepository.save(message);
        kafkaMessageService.send(dto);
    }

    public void leave(ChatMessageDTO.Leave leave, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        redisParticipantsService.leave(leave.getChatId(), userDetails.getUsername());
        graphqlService.decrementPersonnel(leave.getChatId(), userDetails.getUsername(), userDetails.getRole());
        chatUserSubscriptionService.unsubscribe(leave.getChatId(), userDetails.getUsername());

        ChatMessageDTO dto = new ChatMessageDTO(leave, userDetails.getUsername());
        ChatMessage message = new ChatMessage(dto);
        chatMessageRepository.save(message);
        kafkaMessageService.send(dto);
    }
}
