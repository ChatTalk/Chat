package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.document.UserSubscription;
import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.dto.ChatSubscriptionDTO;
import com.example.chatservermessage.domain.repository.ChatReadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j(topic = "ChatReadService")
@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final ChatReadRepository chatReadRepository;

    // 유저가 구독한 채팅방에 새 메시지를 추가
    public void addUnreadMessage(String username, String chatId, ChatMessageDTO unreadMessage) {
        Optional<UserSubscription> userOptional = chatReadRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            UserSubscription userSubscription = userOptional.get();

            // 해당 유저의 구독 채팅방 목록 중에서 chatId에 해당하는 채팅방 찾기
            userSubscription.addUnreadChatMessage(chatId, unreadMessage);
            chatReadRepository.save(userSubscription);
        }
    }

    public void addChatRoom(String username, String chatId) {
        UserSubscription userSubscription;

        if (chatReadRepository.findByUsername(username).isEmpty()) {
            userSubscription = new UserSubscription(username);
        } else {
            userSubscription = chatReadRepository.findByUsername(username).get();
        }

        if (userSubscription.getSubscribedChats()
                .stream().map(ChatSubscriptionDTO::getChatId).toList().contains(chatId)) {
            log.info("이미 구독하고 있는 채팅방");
            return;
        }

        userSubscription.addChatRoom(chatId);
        chatReadRepository.save(userSubscription);
    }

    // 유저 퇴장 시, 해당 채팅 구독 삭제
    public void deleteChatRoom(String username, String chatId) {
        Optional<UserSubscription> userOptional = chatReadRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            UserSubscription userSubscription = userOptional.get();
            userSubscription.deleteChatRoom(chatId);
            chatReadRepository.save(userSubscription);
        }
    }
}
