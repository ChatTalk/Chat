package com.example.chatservermessage.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chatUser")
public class ChatUserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chatRoom")
    private String chatId;

    @Column(name = "subscribedUser")
    private String email;

    public ChatUserSubscription(String chatId, String email) {
        this.chatId = chatId;
        this.email = email;
    }
}
