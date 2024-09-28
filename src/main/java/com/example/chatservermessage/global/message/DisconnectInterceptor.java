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
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.example.chatservermessage.global.constant.Constants.REDIS_PARTICIPATED_KEY;
import static com.example.chatservermessage.global.constant.Constants.REDIS_SUBSCRIBE_KEY;

@Slf4j(topic = "DisconnectInterceptor")
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 101)
public class DisconnectInterceptor implements ChannelInterceptor {

    private final RedisTemplate<String, String> subscribeTemplate;
    private final RedisTemplate<String, String> participatedTemplate;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        log.info("엑세스 명령: {}", accessor.getCommand());

        if (Objects.equals(accessor.getCommand(), StompCommand.DISCONNECT)) {
            String destination = accessor.getDestination(); // 구독할 채널
            log.info("구독 채널: {}", destination);
            String chatRoom = removePrefix(destination);
            log.info("구독 채팅창 방 id: {}", chatRoom);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
                throw new IllegalArgumentException("웹소켓 단계 인증 객체 비어있음");
            }

            String email = ((UserDetailsImpl) authentication.getPrincipal()).getUsername();
            log.info("사용자 정보: {}", email);

            subscribeTemplate.opsForSet().remove(REDIS_SUBSCRIBE_KEY + email, chatRoom);
            participatedTemplate.opsForList().remove(REDIS_PARTICIPATED_KEY + chatRoom, 0, email);
        }

        return message;
    }

    // 구독 채널 접두어 제거 메소드
    private String removePrefix(String destination) {
        String prefix = "/sub/chat/";
        log.info("디스커넥트 시점의 목적지 확인: {}", destination);

        if (destination != null && destination.startsWith(prefix)) {
            return destination.substring(prefix.length());
        }

        throw new IllegalArgumentException("채팅방 구독 경로 설정 확인 필요");
    }
}
