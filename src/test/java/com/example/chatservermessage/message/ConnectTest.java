package com.example.chatservermessage.message;

import com.example.chatservermessage.domain.dto.UserInfoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConnectTest {

    @LocalServerPort
    private int port;

    private StompSession stompSession;

    /**
     * Spring 의 ApplicationContext 에 모킹된 빈을 추가하여, 해당 빈을 주입받는 모든 컴포넌트에서 이 모킹된 객체를 사용
     * 즉, AuthenticationInterceptor 가 이 모킹된 userInfoTemplate 을 주입받은 거임
     */
    @MockBean
    private RedisTemplate<String, UserInfoDTO> userInfoTemplate;

    /**
     * Mockito 를 통해 직접 생성되어 메모리에 존재, ApplicationContext 와는 독립적
     * 특정 메서드의 동작 제어
     */
    @Mock
    private ValueOperations<String, UserInfoDTO> valueOperations;

    private String WS_URL;

    // StompClient 인스턴스 생성
    private WebSocketStompClient getStompClient() {
        WebSocketStompClient stompClient =
                new WebSocketStompClient(new SockJsClient(List.of(
                        new WebSocketTransport(new StandardWebSocketClient()))));

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        return stompClient;
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.WS_URL = "ws://localhost:" + port + "/stomp/chat";
        // RedisTemplate Mock 생성
        when(userInfoTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void test() throws ExecutionException, InterruptedException, TimeoutException {
        // Mock UserInfoDTO
        UserInfoDTO mockUserInfo = new UserInfoDTO(null, "test@example.com", "ROLE_USER", null);

        WebSocketStompClient stompClient = getStompClient();

        // 헤더 세팅
        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
        webSocketHttpHeaders.add("email", "user@example.com");
        webSocketHttpHeaders.add("role", "ROLE_USER");
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer testAccessToken");

        when(valueOperations.get(anyString())).thenReturn(mockUserInfo);

        // WebSocket 세션 연결
        this.stompSession = stompClient
                .connectAsync(WS_URL, webSocketHttpHeaders, stompHeaders, new StompSessionHandlerAdapter() {})
                .get(15, TimeUnit.SECONDS);

        assertTrue(stompSession.isConnected(), "WebSocket 연결 성공");
    }
}
