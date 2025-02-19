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

    public ChatRoomDTO getChatRoomById(String chatId, String email, String role) {
        String query = "query GetChatRoomById($id: ID!) { getChatRoomById(id: $id) { id title openUsername maxPersonnel createdAt } }";

        // GraphQL 요청 보내기
        ChatRoomDTO response =  webGraphQlClient
                .mutate()
                .header("email", email)
                .header("role", role)
                .build()
                .document(query)
                .variable("id", chatId)
                .retrieve("getChatRoomById") // 반환할 필드를 지정
                .toEntity(ChatRoomDTO.class) // DTO로 매핑
                .block(); // STOMP 구조에 따른 톰캣으로 인한 블로킹 처리

        log.info("graphql 응답; {}", response);

        return response;
    }

    public ChatRoomDTO incrementPersonnel(String chatId, String email, String role) {
        // GraphQL Mutation 쿼리 정의
        String mutation = "mutation IncrementPersonnel($id: ID!) { incrementPersonnel(id: $id) { id title openUsername personnel maxPersonnel createdAt } }";

        // GraphQL 요청 보내기
        ChatRoomDTO response = webGraphQlClient
                .mutate()
                .header("email", email)  // 필요한 헤더 추가
                .header("role", role)
                .build()
                .document(mutation)       // GraphQL mutation 설정
                .variable("id", chatId)   // 변수로 chatId 전달
                .retrieve("incrementPersonnel") // 반환 필드 설정
                .toEntity(ChatRoomDTO.class)    // 응답을 ChatRoomDTO로 매핑
                .block();  // 블로킹 방식 처리

        log.info("graphql incrementPersonnel 응답: {}", response);

        return response;
    }

    public ChatRoomDTO decrementPersonnel(String chatId, String email, String role) {
        String mutation = "mutation DecrementPersonnel($id: ID!) { decrementPersonnel(id: $id) { id title openUsername personnel maxPersonnel createdAt } }";

        ChatRoomDTO response = webGraphQlClient
                .mutate()
                .header("email", email)
                .header("role", role)
                .build()
                .document(mutation)
                .variable("id", chatId)
                .retrieve("decrementPersonnel")
                .toEntity(ChatRoomDTO.class)
                .block();

        log.info("graphql decrementPersonnel 응답: {}", response);

        return response;
    }
}
