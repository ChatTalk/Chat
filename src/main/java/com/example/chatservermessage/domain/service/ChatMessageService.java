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

    private final RedisParticipantsService redisParticipantsService;
    private final KafkaMessageService kafkaMessageService;
    private final GraphqlService graphqlService;
    private final ChatUserSubscriptionService chatUserSubscriptionService;

    public void enter(ChatMessageDTO.Enter enter, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String username = userDetails.getUsername();
        String role = userDetails.getRole();

        log.info("사용자 이메일: {}", username);chatUserSubscriptionService.subscribe(enter.getChatId(), username);
        log.info("사용자 권한: {}", role);

        ChatRoomDTO chatRoomDTO = graphqlService.getChatRoomById(enter.getChatId(), username, role);

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
         *
         * 정확히는 구독 처리를 먼저 하되, 거기서 대기열로 제어
         */
        chatUserSubscriptionService.subscribe(enter.getChatId(), username); // 구독 메타데이터 생성(대기열_

        /**
         * 이쪽 로직에서도 아마 대기열 구현이 필요해질듯
         * 레디스에서 RDBMS로 선회하는 거니까
         * facade 패턴 통한 분산 락 구현으로 고고씽
         *
         * 이 로직 내에서 대기열 예외 처리, 즉 아래의 로직은 없어질 예정
         * chatUserSubscriptionService 여기서 대기열 처리를 해야될 테므로
         */
        long participatedPersonnel = chatUserSubscriptionService.countByChatId(enter.getChatId());

        if (chatRoomDTO.maxPersonnel().longValue() == participatedPersonnel) {
            throw new IllegalArgumentException("이미 최대인원 가득참");
        }
        // ... 여기까지 없어질 예정

        redisParticipantsService.participate(enter.getChatId(), username); // 접속 데이터 처리(NoSQL)

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
