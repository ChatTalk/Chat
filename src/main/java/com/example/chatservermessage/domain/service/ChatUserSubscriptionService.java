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

    private final ChatUserSubscriptionRepository chatUserSubscriptionRepository;

    public void subscribe(String chatId, String email) {
        chatUserSubscriptionRepository.save(new ChatUserSubscription(chatId, email));
    }

    public void unsubscribe(String chatId, String email) {
        chatUserSubscriptionRepository.delete(new ChatUserSubscription(chatId, email));
    }
}
