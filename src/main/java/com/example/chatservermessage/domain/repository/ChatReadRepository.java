package com.example.chatservermessage.domain.repository;

import com.example.chatservermessage.domain.document.UserSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatReadRepository extends MongoRepository<UserSubscription, String> {
    // 유저 이름으로 조회
    Optional<UserSubscription> findByUsername(String username);
}
