package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.document.UserSubscription;
import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.dto.ChatSubscriptionDTO;
import com.example.chatservermessage.domain.repository.ChatReadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j(topic = "ChatReadService")
@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final ChatReadRepository chatReadRepository;

    // 도큐먼트 생성
    public void createUserOrUpdate(String username) {
        if (chatReadRepository.findByUsername(username).isEmpty()) {
            UserSubscription userSubscription = new UserSubscription(username);
            chatReadRepository.save(userSubscription);
        }
    }

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

    // 유저가 구독한 채팅방의 읽지 않은 메시지 가져오기
    public List<ChatMessageDTO> getUnreadMessages(String username, String chatId) {
        Optional<UserSubscription> userOptional =  chatReadRepository.findByUsername(username);

        return userOptional.map(userSubscription -> userSubscription.getSubscribedChats().stream()
                .filter(sub -> sub.getChatId().equals(chatId))
                .findFirst()
                .map(ChatSubscriptionDTO::getUnreadMessages)
                .orElse(Collections.emptyList())).orElse(Collections.emptyList());
    }

    // 유저 퇴장 시, 해당 채팅 구독 삭제
    public void deleteChatRoom(String username, String chatId) {
        Optional<UserSubscription> userOptional = chatReadRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            UserSubscription userSubscription = userOptional.get();
            userSubscription.deleteChatRoom(chatId);
        }
    }
}
