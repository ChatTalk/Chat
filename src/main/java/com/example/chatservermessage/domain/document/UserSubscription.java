package com.example.chatservermessage.domain.document;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.dto.ChatSubscriptionDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
public class UserSubscription {
    @Id
    private String id; // MongoDB 기본 키

    private String username;

    private List<ChatSubscriptionDTO> subscribedChats;

    public UserSubscription(String username) {
        this.username = username;
        this.subscribedChats = new ArrayList<>();
    }

    public void addUnreadChatMessage(String chatId, ChatMessageDTO chatMessageDTO) {
        ChatSubscriptionDTO chatSubscriptionDTO = this.subscribedChats
                .stream()
                .filter(e -> e.getChatId().equals(chatId)).findFirst()
                .orElse(null);

        // 구독된 채팅방이 없으면 새로 생성해서 추가
        if (chatSubscriptionDTO == null) {
            chatSubscriptionDTO = new ChatSubscriptionDTO();
            chatSubscriptionDTO.setChatId(chatId);
            chatSubscriptionDTO.setUnreadMessages(new ArrayList<>());
            this.subscribedChats.add(chatSubscriptionDTO);
        }

        chatSubscriptionDTO.getUnreadMessages().add(chatMessageDTO);
    }

    public void deleteChatRoom(String chatId) {
        this.subscribedChats
                .removeIf(chatSubscriptionDTO -> chatSubscriptionDTO.getChatId().equals(chatId));
    }
}
