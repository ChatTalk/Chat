package com.example.chatservermessage.global.message;

import com.example.chatservermessage.domain.dto.UserInfoDTO;
import com.example.chatservermessage.global.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.example.chatservermessage.global.constant.Constants.COOKIE_AUTH_HEADER;
import static com.example.chatservermessage.global.constant.Constants.REDIS_ACCESS_KEY;

@Slf4j(topic = "WebSocketInterceptor")
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketInterceptor implements ChannelInterceptor {

    @Value("${cache.key}")
    private String key;

    private final RedisTemplate<String, UserInfoDTO> userInfoTemplate;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        log.info("엑세스 명령: {}", accessor.getCommand());
        log.info("메세지 헤더 확인 ㅠㅠ: {}", String.valueOf(accessor.getMessageHeaders()));

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("인증 시작");
            this.setAuthenticate(accessor);
        }
        return message;
    }

    private void setAuthenticate(final StompHeaderAccessor accessor) {
        String accessTokenValue = accessor.getFirstNativeHeader(COOKIE_AUTH_HEADER);
        log.info("가지고 온 엑세스 토큰: {}", accessTokenValue);

        if (accessTokenValue == null) {
            log.info("엑세스 토큰 없음...?");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non access token");
        }

        String prefix = "Bearer ";
        String prefixKey = "Bearer%20";
        String accessToken = prefixKey + accessTokenValue.substring(prefix.length());

        log.info("레디스 엑세스 토큰 확인: {}", accessToken);

        String accessTokenKey = createFingerPrint(accessToken);
        log.info("레디스 엑세스 토큰 캐시 키 확인: {}", accessTokenKey);
        UserInfoDTO dto = userInfoTemplate.opsForValue().get(REDIS_ACCESS_KEY + accessTokenKey);

        if (dto == null) {
            log.info("DTO 없음...?");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non cache access token");
        }

        log.info("레디스로부터 가지고 온 DTO: {}", dto);

        // 좀 더 실용적인 인증 수단 마련 필요
        String email = dto.getEmail();
        log.info("소켓 CONNECT 시도, 유저 이메일 : {}", email);

        Authentication authentication = this.createAuthentication(dto);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        accessor.setUser(authentication);
    }

    private Authentication createAuthentication(final UserInfoDTO userInfoDTO) {
        final UserDetails userDetails = new UserDetailsImpl(userInfoDTO.getEmail(), userInfoDTO.getRole());

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    // 디바이스 핑거프린트 생성 메소드
    private String createFingerPrint(String token) {
        String data = key + token;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            // 바이트 배열 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해시 알고리즘 탐색 불가", e);
        }
    }
}