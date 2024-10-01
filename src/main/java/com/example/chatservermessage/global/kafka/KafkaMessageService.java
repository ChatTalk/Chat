package com.example.chatservermessage.global.kafka;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.example.chatservermessage.global.constant.Constants.CHAT_DESTINATION;
import static com.example.chatservermessage.global.constant.Constants.KAFKA_CHAT_TOPIC;

@Slf4j(topic = "KafkaMessageService")
@Service
@RequiredArgsConstructor
public class KafkaMessageService {

    private final KafkaTemplate<String, ChatMessageDTO> kafkaTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    // producer
    public void send(ChatMessageDTO chatMessageDTO) {
        try {
            log.info("채팅 메세지 송신: {}", chatMessageDTO.getMessage());
            kafkaTemplate.send(KAFKA_CHAT_TOPIC, chatMessageDTO.getChatId(), chatMessageDTO);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "SEND Failed");
        }
    }

    // consumer
    @KafkaListener(topics = KAFKA_CHAT_TOPIC)
    public void listen(ChatMessageDTO dto) {
        log.info("채팅 메세지 수신: {}번 // {}", dto.getChatId(), dto.getMessage());
        messagingTemplate.convertAndSend(CHAT_DESTINATION + dto.getChatId(), dto);
    }
}
