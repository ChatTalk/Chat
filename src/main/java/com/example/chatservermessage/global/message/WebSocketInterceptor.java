package com.example.chatservermessage.global.message;

import com.example.chatservermessage.domain.dto.TokenDTO;
import com.example.chatservermessage.domain.dto.UserInfoDTO;
import com.example.chatservermessage.global.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;

import java.time.Duration;
import java.util.UUID;

@Slf4j(topic = "WebSocketInterceptor")
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketInterceptor implements ChannelInterceptor {

    @Value("${kafka.topic}")
    private String topic;

    private final ReactiveKafkaProducerTemplate<String, TokenDTO> kafkaProducerTemplate;
    private final KafkaReceiver<String, UserInfoDTO> kafkaReceiver;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        log.info("일단 여긴 도착했는지?");

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        /**
         * 일단 여긴 레디스 캐시 조회로 고쳐야겠다. 카프카 처리 넘 빡빡할듯
         */

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            return setAuthenticate(accessor)
                    .doOnNext(userInfoDTO -> {
                        if (userInfoDTO != null) {
                            // 인증이 성공한 경우
                            String email = userInfoDTO.getEmail();
                            String role = userInfoDTO.getRole();

                            log.info("소켓 CONNECT 시도, 유저 이메일 : {} // 권한 : {}", email, role);

                            Authentication authentication = this.createAuthentication(userInfoDTO);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            accessor.setUser(authentication);
                        } else {
                            // 인증 실패 시 처리
                            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
                        }
                    })
                    .map(userInfoDTO -> MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders()))
                    .doOnError(ex -> log.error("Error during WebSocket authentication", ex))
                    .block(); // 동기화된 블로킹 작업
        }

//        log.info("message 내용 확인: {}", message.getPayload());
        return message;
    }

    private Mono<UserInfoDTO> setAuthenticate(final StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");
        UUID id = UUID.randomUUID();
        log.info("추출된 토큰: {} // 아이디: {}", token, id);

        TokenDTO tokenDTO = new TokenDTO(id, token);

        // 비동기적으로 Kafka에 인증 요청 전송
        return kafkaProducerTemplate.send(topic, id.toString(), tokenDTO)
                .then(Mono.defer(() -> kafkaReceiver
                        .receive()
                        .filter(record -> record.key().equals(id.toString()))
                        .next()
                        .map(ConsumerRecord::value)
                        .timeout(Duration.ofSeconds(10))
                        .onErrorResume(e -> {
                            // 인증 실패 시 처리
                            log.error("Error during authentication", e);
                            return Mono.empty(); // 인증 실패 시 빈 Mono를 반환
                        })
                ));
    }

    private Authentication createAuthentication(final UserInfoDTO userInfoDTO) {
        final UserDetails userDetails = new UserDetailsImpl(userInfoDTO);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}