package com.gliesestudio.phantom.controller;

import com.gliesestudio.phantom.dto.ChatRequest;
import com.gliesestudio.phantom.service.ConversationContextHolder;
import com.gliesestudio.phantom.service.agant.ConversationAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ConversationAgent conversationAgent;

    private final ConversationContextHolder conversationContextHolder;

    @PostMapping
    public ResponseEntity<Map<String, ?>> chat(@RequestBody ChatRequest chatRequest) {
        log.info("Received chat request: {}", chatRequest);

        conversationContextHolder.setConversationId(chatRequest.getConversationId());

        Map<String, ?> response = conversationAgent.runWithContext(chatRequest.getMessage(), new HashMap<>());

        return ResponseEntity.ok(response);
    }

}