package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.dto.ChatRoomDTO;
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

//    private final RedisTemplate<String, Integer> maxPersonnelTemplate;

    private final RedisParticipantsService redisParticipantsService; // 일단 네가 삭제될 예정
    private final KafkaMessageService kafkaMessageService;
    private final GraphqlService graphqlService;
    private final ChatUserSubscriptionService chatUserSubscriptionService;

    public void enter(ChatMessageDTO.Enter enter, @AuthenticationPrincipal UserDetailsImpl userDetails) {
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
            redisParticipantsService.participate(enter.getChatId(), username);
            return;
        }

        /**
         * 채팅창 구독 접속에 대한 예외 로직을 Participant 인스턴스에서 처리해야 함
         * 거기서 만약 인원 초과로 안됐다? 그럼 이쪽 로직들도 안 돌아가고 예외 던져야 하는데
         * 그 에러를 전파받아서 여기서 분산 트랜잭션 처리를 하는 건가? 일단은 로직 구현 해보자
         */
        redisParticipantsService.participate(enter.getChatId(), username);

        Long participatedPersonnel = redisParticipantsService.getSize(enter.getChatId());

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

    public void leave(ChatMessageDTO.Leave leave, Principal principal) {
        redisParticipantsService.leave(leave.getChatId(), principal.getName());
        chatUserSubscriptionService.unsubscribe(leave.getChatId(), principal.getName());

        ChatMessageDTO dto = new ChatMessageDTO(leave, principal.getName());
        kafkaMessageService.send(dto);
    }
}
