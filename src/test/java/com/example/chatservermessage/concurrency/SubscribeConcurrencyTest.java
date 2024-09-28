package com.example.chatservermessage.concurrency;

import com.example.chatservermessage.domain.controller.ChatMessageController;
import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.*;

import static com.example.chatservermessage.global.constant.Constants.REDIS_MAX_PERSONNEL_KEY;
import static com.example.chatservermessage.global.constant.Constants.REDIS_PARTICIPATED_KEY;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 테스트를 수행할 때도 설정 서버가 켜져있어야 함(유레카 서버 too... 도커 redis는 말할 것도 없고...)
 * 인터셉터가 로그에 찍히고 있음. 즉, 인증 객체를 만들 때 관련해서 인터셉터 로직을 추가로 할당해야 될 것 같음
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscribeConcurrencyTest {

    // 로그 기록
    private final Logger log = LoggerFactory.getLogger(getClass());

    @LocalServerPort
    private int port;

    @Autowired
    private RedisTemplate<String, Integer> maxPersonnelTemplate;
    @Autowired
    private RedisTemplate<String, String> participatedTemplate;
    @Autowired
    private ChatMessageController chatMessageController;

    private String url;
    private WebSocketStompClient stompClient;

    // 랜덤 포트 할당
    @BeforeEach
    void setUp() {
        this.url = String.format("ws://localhost:%d/stomp/chat", port);
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        maxPersonnelTemplate.opsForValue().set(REDIS_MAX_PERSONNEL_KEY + "test", 5);
        participatedTemplate.opsForList()
                .rightPush(REDIS_PARTICIPATED_KEY + "test", "HOST");
    }

    @Test
    void testEnterChatRoomConcurrency() throws Exception {
        // given
        int numberOfClients = 1000;  // 1000명의 클라이언트 동시 입장 테스트
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfClients);
        CountDownLatch latch = new CountDownLatch(numberOfClients);

        // when
        // 여러 클라이언트 동시 접속을 위한 Future 리스트
        List<Future<Void>> futures = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numberOfClients; i++) {
            String username = "test" + i;

            Future<Void> future = executorService.submit(() -> {
                try {
                    // Stomp 세션 연결
                    StompSession session = stompClient.connectAsync(
                            url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);


                    // 가짜 Principal (사용자 인증) 생성
                    Principal principal = () -> username;

                    // 채팅방 입장 메시지 생성
                    ChatMessageDTO.Enter enterMessage = new ChatMessageDTO.Enter("test");
                    chatMessageController.enter(enterMessage, principal);
                } finally {
                    latch.countDown();
                }
                return null;
            });

            futures.add(future);
        }

        // 모든 작업이 끝날 때까지 대기
        latch.await();

        // then
        // 참여 인원 확인
        Long participatedPersonnel =
                participatedTemplate.opsForList().size(REDIS_PARTICIPATED_KEY + "test");
        assertNotNull(participatedPersonnel, "참여 인원 저장 오류");

        // 최대 인원(5명)만 참여해야 함
        assertNotEquals(5L, participatedPersonnel, "참여 인원 수 다름, 동시성 이슈 발생");
        log.info("저장 값: {}", participatedPersonnel);

        // 남은 future 확인
        for (Future<Void> future : futures) {
            future.get();  // 예외 발생 여부 확인
        }

        executorService.shutdown();
    }

    @AfterEach
    void tearDown() {
        maxPersonnelTemplate.delete(REDIS_MAX_PERSONNEL_KEY + "test");
        participatedTemplate.delete(REDIS_PARTICIPATED_KEY + "test");
    }
}
