package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.global.kafka.KafkaMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Objects;

import static com.example.chatservermessage.global.constant.Constants.*;

@Slf4j(topic = "ChatMessageService")
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final RedisTemplate<String, Integer> maxPersonnelTemplate;

    private final RedisParticipantsService redisParticipantsService;
    private final KafkaMessageService kafkaMessageService;

    public void enter(ChatMessageDTO.Enter enter, Principal principal) throws JsonProcessingException {
        if (redisParticipantsService.checkParticipants(enter.getChatId(), principal.getName())) {
            log.info("이미 해당 {}번 채팅방 구독 중인 유저 {}:", enter.getChatId(), principal.getName());
            redisParticipantsService.updateParticipants("PUT", enter.getChatId(), principal.getName());
            return;
        }

        redisParticipantsService.updateParticipants("PUT", enter.getChatId(), principal.getName());

        Integer maxPersonnel = maxPersonnelTemplate.opsForValue().get(REDIS_MAX_PERSONNEL_KEY + enter.getChatId());
        Long participatedPersonnel = redisParticipantsService.getSize(enter.getChatId());

        // 인원이 초과된 채팅창 접속(예외 처리)
        if (Objects.isNull(maxPersonnel)) {
            throw new IllegalArgumentException("maxPersonnel 에 이상 있다!");
        }

        if (maxPersonnel.longValue() == participatedPersonnel) {
            throw new IllegalArgumentException("이미 최대인원 가득참");
        }

        ChatMessageDTO dto = new ChatMessageDTO(enter, principal.getName());
        kafkaMessageService.send(dto);
    }

    public void message(ChatMessageDTO.Send send, Principal principal) {
        ChatMessageDTO dto = new ChatMessageDTO(send, principal.getName());
        kafkaMessageService.send(dto);
    }

    public void leave(ChatMessageDTO.Leave leave, Principal principal) throws JsonProcessingException {
        redisParticipantsService.updateParticipants("DELETE", leave.getChatId(), principal.getName());

//        log.info("222여기까지는 아이디가 살아있나?: {}", leave.getChatId());

        ChatMessageDTO dto = new ChatMessageDTO(leave, principal.getName());
        kafkaMessageService.send(dto);
    }
}
