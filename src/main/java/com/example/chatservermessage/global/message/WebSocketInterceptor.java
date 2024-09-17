package com.example.chatservermessage.global.message;

import com.example.chatservermessage.domain.dto.UserInfoDTO;
import com.example.chatservermessage.global.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j(topic = "WebSocketInterceptor")
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            this.setAuthenticate(accessor);
        }
        return message;
    }

    private void setAuthenticate(final StompHeaderAccessor accessor) {
        String email = accessor.getFirstNativeHeader("email");
        String role = accessor.getFirstNativeHeader("role");

        UserInfoDTO userInfoDTO = new UserInfoDTO(email, role);

        log.info("소켓 CONNECT 시도, 유저 이메일 : {} // 권한 : {}", email, role);

        Authentication authentication = this.createAuthentication(userInfoDTO);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        accessor.setUser(authentication);
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