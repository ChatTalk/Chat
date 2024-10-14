package com.example.chatservermessage.domain.repository;

import com.example.chatservermessage.domain.entity.ChatUserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 서비스 로직들만 포함시키기(조회 로직은 chat 인스턴스에서)
@Repository
public interface ChatUserSubscriptionRepository extends JpaRepository<ChatUserSubscription, Long> {
    void deleteByChatIdAndEmail(String chatId, String email); // 해당 채팅방 구독 종료에서 써먹기

    boolean existsByChatIdAndEmail(String chatId, String email);
}
