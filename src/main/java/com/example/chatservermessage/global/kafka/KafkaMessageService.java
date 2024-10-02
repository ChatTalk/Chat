package com.example.chatservermessage.global.kafka;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
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

    /**
     * 카프카 수동 커밋 관련 내용
     * -> 이걸 활용해서 접속하지 않았을 때는 메세지 안 읽게 하고 접속해서 채팅창에 접속했을 떄가 되어야 메세지 소비를 가능케 하는..? 가능하려나?
     * https://dkswnkk.tistory.com/744
     *
     수동 커밋은 메시지를 커밋할 시점을 제어할 수 있지만, 나중에 메시지를 읽는 기능은 별도의 저장 및 관리 로직이 필요합니다.
     사용자가 메시지를 나중에 읽을 수 있도록 하려면, 수신한 메시지를 Redis나 데이터베이스에 저장하고, 사용자가 접속했을 때 이 메시지를 불러오는 로직을 구현해야 합니다.
     */
    // consumer
    @KafkaListener(topics = KAFKA_CHAT_TOPIC)
    public void listen(ChatMessageDTO dto) {
        log.info("채팅 메세지 수신: {}번 // {}", dto.getChatId(), dto.getMessage());
        messagingTemplate.convertAndSend(CHAT_DESTINATION + dto.getChatId(), dto);
    }
}
