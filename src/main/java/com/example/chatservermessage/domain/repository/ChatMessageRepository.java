package com.example.chatservermessage.domain.repository;

import com.example.chatservermessage.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 네이티브 쿼리 적용
    @Query(value = "SELECT * FROM chat_message WHERE chat_id = :chatId AND created >= :startTime AND created <= :endTime", nativeQuery = true)
    List<ChatMessage> findMessagesByChatIdAndTimeRange(String chatId, LocalDateTime startTime, LocalDateTime endTime);

}
