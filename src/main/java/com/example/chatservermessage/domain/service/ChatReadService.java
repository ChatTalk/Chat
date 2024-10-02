package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.document.User;
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

    // 유저가 구독한 채팅방에 새 메시지를 추가
    public void addUnreadMessage(String userId, String chatId, ChatMessageDTO unreadMessage) {
        Optional<User> userOptional = chatReadRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 해당 유저의 구독 채팅방 목록 중에서 chatId에 해당하는 채팅방 찾기
            user.getSubscribedChats().stream()
                    .filter(sub -> sub.getChatId().equals(chatId))
                    .findFirst() // 얘는 필요할 것 같진 않지만 일단 추가
                    .ifPresent(sub -> {
                        sub.getUnreadMessages().add(unreadMessage); // 읽지 않은 메시지 추가
                        chatReadRepository.save(user); // MongoDB에 업데이트
                    });
        }
    }

    // 유저가 구독한 채팅방의 읽지 않은 메시지 가져오기
    public List<ChatMessageDTO> getUnreadMessages(String userId, String chatId) {
        Optional<User> userOptional =  chatReadRepository.findById(userId);

        return userOptional.map(user -> user.getSubscribedChats().stream()
                .filter(sub -> sub.getChatId().equals(chatId))
                .findFirst()
                .map(ChatSubscriptionDTO::getUnreadMessages)
                .orElse(Collections.emptyList())).orElse(Collections.emptyList());
    }
}
