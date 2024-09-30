package com.example.chatservermessage.concurrency;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.dto.UserInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;
import java.util.concurrent.*;

import static com.example.chatservermessage.global.constant.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j(topic = "Concurrency_Subscribe")
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscribeTest {

    @LocalServerPort
    private int port;

    @MockBean
    private RedisTemplate<String, UserInfoDTO> userInfoTemplate;
    @Mock
    private ValueOperations<String, UserInfoDTO> valueOperations;

    @Autowired
    private RedisTemplate<String, Integer> maxPersonnelTemplate;
    @Autowired
    private RedisTemplate<String, String> participatedTemplate;
    @Autowired
    private RedisTemplate<String, String> subscribeTemplate;

    private String WS_URL;
    private static final int CLIENT = 1_000;
    private static final String CHAT_ID = "TEST";
    private static final int LIMIT = 5;

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

        maxPersonnelTemplate.opsForValue().set(REDIS_MAX_PERSONNEL_KEY + CHAT_ID, LIMIT);
        participatedTemplate.opsForList().rightPush(REDIS_PARTICIPATED_KEY + CHAT_ID, "Host");

        // RedisTemplate Mock 생성
        when(userInfoTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("구독 및 관련 송신 메세지 시점에서의 동시성 테스트")
    void test() throws ExecutionException, InterruptedException, TimeoutException {
        ExecutorService executorService = Executors.newFixedThreadPool(CLIENT);
        CountDownLatch latch = new CountDownLatch(CLIENT);

        for (int i = 1; i <= CLIENT; i++) {
            // Mock UserInfoDTO
            String username = "test" + i;
            UserInfoDTO mockUserInfo = new UserInfoDTO(null, username, "ROLE_USER", null);

            WebSocketStompClient stompClient = getStompClient();

            // 헤더 세팅
            WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
            webSocketHttpHeaders.add("email", username);
            webSocketHttpHeaders.add("role", "ROLE_USER");
            StompHeaders stompHeaders = new StompHeaders();
            stompHeaders.add("Authorization", "Bearer testAccessToken");

            when(valueOperations.get(anyString())).thenReturn(mockUserInfo);

            // WebSocket 세션 연결
            StompSession stompSession = stompClient
                    .connectAsync(WS_URL, webSocketHttpHeaders, stompHeaders, new StompSessionHandlerAdapter() {
                    })
                    .get(15, TimeUnit.SECONDS);

            log.info("가상 사용자 {} 웹소켓 연결 여부: {}", username, stompSession.isConnected());

            ChatMessageDTO.Enter enter = new ChatMessageDTO.Enter(CHAT_ID);

            executorService.submit(() -> {
                        try {
                            stompSession.send("/send/chat/enter", enter);
                        } catch (Exception e) {
                            log.info("구독 완료 실패: test_{}", username);
                            log.error(e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    }
            );
        }

        latch.await();

        Integer maxPersonnel = maxPersonnelTemplate.opsForValue().get(REDIS_MAX_PERSONNEL_KEY + CHAT_ID);
        Long participatedPersonnel = participatedTemplate.opsForList().size(REDIS_PARTICIPATED_KEY + CHAT_ID);

        log.info("최대 인원: {}", maxPersonnel);
        log.info("참여 인원: {}", participatedPersonnel);

        assertEquals(maxPersonnel.longValue(), participatedPersonnel);
    }

    @AfterEach
    void testDown() {
        maxPersonnelTemplate.delete(REDIS_MAX_PERSONNEL_KEY + CHAT_ID);
        participatedTemplate.delete(REDIS_PARTICIPATED_KEY + CHAT_ID);

        for (int i = 1; i <= LIMIT; i++) {
            String username = "test" + i;
            subscribeTemplate.delete(REDIS_SUBSCRIBE_KEY + username);
        }
    }
}
