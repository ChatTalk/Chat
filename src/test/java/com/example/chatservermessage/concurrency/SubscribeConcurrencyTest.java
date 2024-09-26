package com.example.chatservermessage.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscribeConcurrencyTest {

    // 로그 기록
    private final Logger log = LoggerFactory.getLogger(getClass());

    @LocalServerPort
    private int port;

    private String url;

    // StompClient 인스턴스 생성 메소드
    private WebSocketStompClient getStompClient() {
        // WebSocketStompClient 인스턴스 설정
        // SockJS 사용 고려
        WebSocketStompClient stompClient =
                new WebSocketStompClient(new SockJsClient(List.of(
                        new WebSocketTransport(new StandardWebSocketClient()))));

        // JSON 직렬화 및 역직렬화를 위한 converter 설정
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        return stompClient;
    }

    // 랜덤 포트 할당
    @BeforeEach
    void setUp() {
        this.url = String.format("ws://localhost:%d/stomp/chat", port);
    }
}
