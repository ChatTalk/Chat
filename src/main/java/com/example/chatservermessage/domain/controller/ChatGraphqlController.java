package com.example.chatservermessage.domain.controller;

import com.example.chatservermessage.domain.dto.ChatMessageDTO;
import com.example.chatservermessage.domain.dto.GraphqlMessageDTO;
import com.example.chatservermessage.domain.dto.GraphqlSubscriptionDTO;
import com.example.chatservermessage.domain.entity.ChatMessage;
import com.example.chatservermessage.domain.repository.ChatMessageRepository;
import com.example.chatservermessage.domain.service.ChatUserSubscriptionService;
import com.example.chatservermessage.domain.utility.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/graphql")
public class ChatGraphqlController {

    private final ChatUserSubscriptionService chatUserSubscriptionService;
    private final ChatMessageRepository chatMessageRepository;

    @QueryMapping
    public List<GraphqlSubscriptionDTO> subscriptionsByEmail(@Argument String email) {
        return chatUserSubscriptionService.getSubscriptions(email);
    }

    @QueryMapping
    public List<GraphqlMessageDTO> getChatMessages(@Argument String chatId, @Argument String exitTime) {
        LocalDateTime exit = DateTimeUtil.toLocalDateTime(exitTime);

        log.info("받은 퇴장 타임: {}", exit);

        List<ChatMessage> chatMessages = chatMessageRepository.findMessagesByChatIdAndTimeRange(
                chatId, exit, LocalDateTime.now()
        );

        return chatMessages.stream()
                .map(e -> new GraphqlMessageDTO(
                        e.getChatId(),
                        e.getType(),
                        e.getUsername(),
                        e.getMessage(),
                        DateTimeUtil.toStringTime(e.getCreatedAt()))
                ).toList();
    }
}
