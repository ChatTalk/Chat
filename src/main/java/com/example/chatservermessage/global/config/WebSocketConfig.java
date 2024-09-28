package com.example.chatservermessage.global.config;

import com.example.chatservermessage.global.message.DisconnectInterceptor;
import com.example.chatservermessage.global.message.SubscribeInterceptor;
import com.example.chatservermessage.global.message.AuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final SubscribeInterceptor subscribeInterceptor;

    /**
     * disconnect 에 직접적으로 파라미터를 담아 보내 낚아챌 수 없기 때문에...
     */
//    private final DisconnectInterceptor disconnectInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/send");
        registry.enableSimpleBroker("/sub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/stomp/chat")
                .setAllowedOriginPatterns("http://localhost:3000") // 중복 cors 설정 방지
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration
                .interceptors(authenticationInterceptor)
                .interceptors(subscribeInterceptor);
//                .interceptors(disconnectInterceptor);
    }
}
