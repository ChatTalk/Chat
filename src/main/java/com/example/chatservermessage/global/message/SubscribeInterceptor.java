package com.example.chatservermessage.global.message;

import com.example.chatservermessage.global.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.example.chatservermessage.global.constant.Constants.*;

@Slf4j(topic = "SubscribeInterceptor")
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class SubscribeInterceptor implements ChannelInterceptor {

    private final RedisTemplate<String, String> subscribeTemplate;
    private final RedisTemplate<String, Integer> maxPersonnelTemplate;
    private final RedisTemplate<String, String> participatedTemplate;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        log.info("엑세스 명령: {}", accessor.getCommand());

        if (Objects.equals(accessor.getCommand(), StompCommand.SUBSCRIBE)) {
            String destination = accessor.getDestination(); // 구독할 채널
            log.info("구독 채널: {}", destination);
            String chatRoom = removePrefix(destination);
            log.info("구독 채팅창 방 id: {}", chatRoom);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
                return createErrorMessage("웹소켓 인증 실패", accessor);
            }

            String email = ((UserDetailsImpl) authentication.getPrincipal()).getUsername();
            log.info("사용자 정보: {}", email);

            /**
             * redis template 관련 예외 설정(NPE 방지) 더 빡세게 할 필요 이씀
             */

            if (maxPersonnelTemplate.opsForValue().get(REDIS_MAX_PERSONNEL_KEY + chatRoom)
            <= participatedTemplate.opsForList().size(REDIS_PARTICIPATED_KEY + chatRoom)
            ) {
                log.info("이미 가득찬 채팅방");
                return createErrorMessage("이미 채팅방이 가득 찼음", accessor);
            }

            return message;
        }

        return message;
    }

    // 구독 채널 접두어 제거 메소드
    private String removePrefix(String destination) {
        String prefix = "/sub/chat/";

        if (destination != null && destination.startsWith(prefix)) {
            return destination.substring(prefix.length());
        }

        throw new IllegalArgumentException("채팅방 구독 경로 설정 확인 필요");
    }

    // ERROR 메시지를 생성하는 헬퍼 메서드
    private Message<?> createErrorMessage(String errorMessage, StompHeaderAccessor accessor) {
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setMessage(errorMessage);
        errorAccessor.setSessionId(accessor.getSessionId());

        return MessageBuilder.createMessage(new byte[0], errorAccessor.getMessageHeaders());
    }
}
