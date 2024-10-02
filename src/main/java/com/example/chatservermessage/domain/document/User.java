package com.example.chatservermessage.domain.document;

import com.example.chatservermessage.domain.dto.ChatSubscriptionDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id; // MongoDB 기본 키

    private String username;

    private List<ChatSubscriptionDTO> subscribedChats;
}
