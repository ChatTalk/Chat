package com.example.chatservermessage.global.kafka;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.example.chatservermessage.global.constant.Constants.*;

@Slf4j(topic = "KafkaMessageService")
@Service
@RequiredArgsConstructor
public class KafkaMessageService {

    private final KafkaTemplate<String, ChatMessageDTO> kafkaTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    // producer
    public void send(ChatMessageDTO chatMessageDTO) {
        try {
            log.info("채팅방 아이디: {}", chatMessageDTO.getChatId());
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
    /**
     * 카프카 리스너가 비동기적으로 동작해서 인증 객체가 생성된 시점의 스레드와 카프카 리스너의 스레드가 다름
     * 그래서 인증 객체가 null...
     * 다만 앱 레벨에서 카프카 리스너 메소드는 싱글 스레드로 동작한다. 카프카가 멀티 스레드로 동작
     */
    public void listen(ChatMessageDTO dto) {
        log.info("채팅 메세지 수신: {}번 // {}", dto.getChatId(), dto.getMessage());
//        Map<Object, Object> entries
//                = participatedTemplate.opsForHash()
//                .entries(REDIS_PARTICIPATED_KEY + dto.getChatId());
//
//        entries.forEach((key, value) -> {
//            if (value == Boolean.FALSE) {
//                log.info("다른 채팅창에 있거나 접속하지 않은 유저: {}", key);
//                chatReadService.addUnreadMessage((String) key, dto.getChatId(), dto);
//            } else {
//                log.info("현재 접속 중인 유저: {}", key);
//            }
//        });

        messagingTemplate.convertAndSend(CHAT_DESTINATION + dto.getChatId(), dto);
    }
}
