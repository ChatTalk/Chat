package com.example.chatservermessage.domain.service;

import com.example.chatservermessage.domain.dto.ChatRoomDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.WebGraphQlClient;
import org.springframework.stereotype.Service;

@Slf4j(topic = "GRAPHQL_SERVICE")
@Service
@RequiredArgsConstructor
public class GraphqlService {

    private final WebGraphQlClient webGraphQlClient;

    public ChatRoomDTO getChatRoomById(String chatId) {
        String query = "query GetChatRoomById($id: ID!) { getChatRoomById(id: $id) { id title openUsername maxPersonnel createdAt } }";

        // GraphQL 요청 보내기
        return webGraphQlClient
                .document(query)
                .variable("id", chatId)
                .retrieve("getChatRoomById") // 반환할 필드를 지정
                .toEntity(ChatRoomDTO.class) // DTO로 매핑
                .block();
    }

}
