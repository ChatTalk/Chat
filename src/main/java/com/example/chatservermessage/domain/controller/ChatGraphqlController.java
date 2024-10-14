package com.example.chatservermessage.domain.controller;

import com.example.chatservermessage.domain.dto.GraphqlDTO;
import com.example.chatservermessage.domain.service.ChatUserSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/graphql")
public class ChatGraphqlController {

    private final ChatUserSubscriptionService chatUserSubscriptionService;

    @QueryMapping
    public List<GraphqlDTO> getSubscriptions(@Argument String email) {
        return chatUserSubscriptionService.getSubscriptions(email);
    }
}
