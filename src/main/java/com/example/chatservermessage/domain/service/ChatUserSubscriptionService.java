package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.dto.GraphqlDTO;
import com.example.chatservermessage.domain.entity.ChatUserSubscription;
import com.example.chatservermessage.domain.repository.ChatUserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatUserSubscriptionService {

    // 왠지 대기열 구현이 필요할 것 같은데?

    private final ChatUserSubscriptionRepository chatUserSubscriptionRepository;

    // 구독 여부
    public boolean isSubscribed(String chatId, String email) {
        return chatUserSubscriptionRepository.existsByChatIdAndEmail(chatId, email);
    }

    // 구독
    public void subscribe(String chatId, String email) {
        chatUserSubscriptionRepository.save(new ChatUserSubscription(chatId, email));
    }

    // 구독 종료
    public void unsubscribe(String chatId, String email) {
        chatUserSubscriptionRepository.deleteByChatIdAndEmail(chatId, email);
    }

    // 개별 이메일 당 구독 리스트 들고 오기
    public List<GraphqlDTO> getSubscriptions(String email) {
        log.info("그래프큐엘 이메일: {}", email);

        List<ChatUserSubscription> subscriptions = chatUserSubscriptionRepository.findByEmail(email);
        log.info("데이터: {}", subscriptions);

        return chatUserSubscriptionRepository.findByEmail(email).
                stream()
                .map(e -> new GraphqlDTO(e.getId().toString(), e.getChatId(), e.getEmail()))
                .toList();
    }

    // 구독 리스트 길이 갖고오기

    /**
     * 말했듯이 임시 로직
     * 대기열 구현하고 나서 사라질 예정
     * @param chatId
     * @return
     */
    public long countByChatId(String chatId) {
        return chatUserSubscriptionRepository.countByChatId(chatId);
    }
}
