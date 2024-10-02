package com.example.chatservermessage.domain.repository;

import com.example.chatservermessage.domain.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatReadRepository extends MongoRepository<User, String> {
    // 유저 이름으로 조회
    Optional<User> findByUsername(String username);
}
