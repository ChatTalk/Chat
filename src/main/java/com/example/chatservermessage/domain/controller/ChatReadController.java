package com.example.chatservermessage.domain.controller;

import com.example.chatservermessage.domain.service.ChatReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class ChatReadController {

    private final ChatReadService chatReadService;
}
