package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.entity.ChatUserSubscription;
import com.example.chatservermessage.domain.repository.ChatUserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        chatUserSubscriptionRepository.delete(new ChatUserSubscription(chatId, email));
    }
}
