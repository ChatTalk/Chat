package com.example.chatservermessage.domain.repository;

import com.example.chatservermessage.domain.entity.ChatUserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 서비스 로직들만 포함시키기(조회 로직은 chat 인스턴스에서)
@Repository
public interface ChatUserSubscriptionRepository extends JpaRepository<ChatUserSubscription, Long> {
    void deleteByChatIdAndEmail(String chatId, String email); // 해당 채팅방 구독 종료에서 써먹기

    boolean existsByChatIdAndEmail(String chatId, String email);

    /**
     * 임시 로직임, 대기열 구현 후에 없어질 예정
     * @param chatId
     * @return
     */
    long countByChatId(String chatId);
}
