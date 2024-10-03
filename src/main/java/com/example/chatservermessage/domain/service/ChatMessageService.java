package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.global.kafka.KafkaMessageService;
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
//    private final RedisTemplate<String, String> subscribeTemplate; // 삭제 예정
    private final RedisTemplate<String, Boolean> participatedTemplate;

    private final KafkaMessageService kafkaMessageService;
    private final ChatReadService chatReadService;

    public void enter(ChatMessageDTO.Enter enter, Principal principal) {
        // 이미 구독 중인 유저의 재접속
//        if (Boolean.TRUE.equals(subscribeTemplate.opsForSet().isMember(REDIS_SUBSCRIBE_KEY + principal.getName(), enter.getChatId()))) {
//            log.info("이미 해당 {}번 채팅방 구독 중인 유저 {}:", enter.getChatId(), principal.getName());
//            return;
//        }
        participatedTemplate.opsForHash()
                .put(REDIS_PARTICIPATED_KEY + enter.getChatId(), principal.getName(), true);

        if (participatedTemplate.opsForHash().hasKey(REDIS_PARTICIPATED_KEY + enter.getChatId(), principal.getName()) == Boolean.TRUE) {
            log.info("이미 해당 {}번 채팅방 구독 중인 유저 {}:", enter.getChatId(), principal.getName());
            return;
        }

        Integer maxPersonnel = maxPersonnelTemplate.opsForValue().get(REDIS_MAX_PERSONNEL_KEY + enter.getChatId());
        Long participatedPersonnel = participatedTemplate.opsForHash().size(REDIS_PARTICIPATED_KEY + enter.getChatId());

        // 인원이 초과된 채팅창 접속(예외 처리)
        if (Objects.isNull(participatedPersonnel) || Objects.isNull(maxPersonnel)) {
            throw new IllegalArgumentException("participatedPersonnel 혹은 maxPersonnel 에 이상 있다!");
        }

        if (maxPersonnel.longValue() == participatedPersonnel) {
            throw new IllegalArgumentException("이미 최대인원 가득참");
        }

//        subscribeTemplate.opsForSet()
//                .add(REDIS_SUBSCRIBE_KEY + principal.getName(), enter.getChatId());

        ChatMessageDTO dto = new ChatMessageDTO(enter, principal.getName());
        kafkaMessageService.send(dto);
    }

    public void message(ChatMessageDTO.Send send, Principal principal) {
        ChatMessageDTO dto = new ChatMessageDTO(send, principal.getName());
        kafkaMessageService.send(dto);
    }

    public void leave(ChatMessageDTO.Leave leave, Principal principal) {
//        subscribeTemplate.opsForSet()
//                .remove(REDIS_SUBSCRIBE_KEY + principal.getName(), leave.getChatId());
        participatedTemplate.opsForHash()
                .delete(REDIS_PARTICIPATED_KEY + leave.getChatId(), principal.getName());

        ChatMessageDTO dto = new ChatMessageDTO(leave, principal.getName());
        kafkaMessageService.send(dto);
    }
}
