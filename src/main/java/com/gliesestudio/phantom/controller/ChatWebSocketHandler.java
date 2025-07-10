package com.gliesestudio.phantom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gliesestudio.phantom.service.ConversationContextHolder;
import com.gliesestudio.phantom.service.agant.ConversationAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    private final ConversationAgent conversationAgent;

    private final ConversationContextHolder conversationContextHolder;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection established with session ID: {}", session.getId());
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Echo the message to all connected clients (broadcast)
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                log.info("Received chat request: {}", message);

                conversationContextHolder.setConversationId(UUID.fromString(session.getId()));
                Map<String, ?> response = conversationAgent.runWithContext(message.getPayload(), new HashMap<>());

                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
        }
        // Here you can integrate with your AI chat service and send the response back
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Connection closed with session ID: {}", session.getId());
        sessions.remove(session);
    }
}

