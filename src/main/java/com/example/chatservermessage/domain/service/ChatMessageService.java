package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.dto.ChatRoomDTO;
import com.example.chatservermessage.global.kafka.KafkaMessageService;
import com.example.chatservermessage.global.user.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Objects;

import static com.example.chatservermessage.global.constant.Constants.*;

@Slf4j(topic = "ChatMessageService")
@Service
@RequiredArgsConstructor
public class ChatMessageService {

//    private final RedisTemplate<String, Integer> maxPersonnelTemplate;

    private final RedisParticipantsService redisParticipantsService; // 일단 네가 삭제될 예정
    private final KafkaMessageService kafkaMessageService;
    private final GraphqlService graphqlService;
    private final ChatUserSubscriptionService chatUserSubscriptionService;

    public void enter(ChatMessageDTO.Enter enter, @AuthenticationPrincipal UserDetailsImpl userDetails) throws JsonProcessingException {
        String username = userDetails.getUsername();
        String role = userDetails.getRole();

        log.info("사용자 이메일: {}", username);
        log.info("사용자 권한: {}", role);

        ChatRoomDTO chatRoomDTO = graphqlService.getChatRoomById(enter.getChatId(), username, role);
        chatUserSubscriptionService.subscribe(enter.getChatId(), username);
//        log.info(
//                "{}번, 제목: {}, 개설자: {}, 제한인원: {}, 개설일자: {}",
//                chatRoomDTO.id(), chatRoomDTO.title(), chatRoomDTO.openUsername(),
//                chatRoomDTO.maxPersonnel(), chatRoomDTO.createdAt()
//        );

        /**
         * 메타데이터(채팅방 아이디, 이메일)까지는 RDBMS의 영역으로 놔두고
         * 실시간으로 내보내게 하기 위한 업데이트 작업은 MongoDB(chatId 안에 email, boolean 이런 식으로)로 처리하자
         */
        if (chatUserSubscriptionService.isSubscribed(enter.getChatId(), username)) {
            log.info("이미 해당 {}번 채팅방 구독 중인 유저 {}:", enter.getChatId(), username);
        }


        if (redisParticipantsService.checkParticipants(enter.getChatId(), username)) {
            log.info("이미 해당 {}번 채팅방 구독 중인 유저 {}:", enter.getChatId(), username);
            redisParticipantsService.updateParticipants("PUT", enter.getChatId(), username);
            return;
        }

        redisParticipantsService.updateParticipants("PUT", enter.getChatId(), username);

//        Integer maxPersonnel = maxPersonnelTemplate.opsForValue().get(REDIS_MAX_PERSONNEL_KEY + enter.getChatId());
        Long participatedPersonnel = redisParticipantsService.getSize(enter.getChatId());

        // 인원이 초과된 채팅창 접속(예외 처리)
//        if (Objects.isNull(maxPersonnel)) {
//            throw new IllegalArgumentException("maxPersonnel 에 이상 있다!");
//        }

        if (chatRoomDTO.maxPersonnel().longValue() == participatedPersonnel) {
            throw new IllegalArgumentException("이미 최대인원 가득참");
        }

        ChatMessageDTO dto = new ChatMessageDTO(enter, username);
        kafkaMessageService.send(dto);
    }

    public void message(ChatMessageDTO.Send send, Principal principal) {
        ChatMessageDTO dto = new ChatMessageDTO(send, principal.getName());
        kafkaMessageService.send(dto);
    }

    public void leave(ChatMessageDTO.Leave leave, Principal principal) throws JsonProcessingException {
        redisParticipantsService.updateParticipants("DELETE", leave.getChatId(), principal.getName());

        chatUserSubscriptionService.unsubscribe(leave.getChatId(), principal.getName());

        ChatMessageDTO dto = new ChatMessageDTO(leave, principal.getName());
        kafkaMessageService.send(dto);
    }
}
