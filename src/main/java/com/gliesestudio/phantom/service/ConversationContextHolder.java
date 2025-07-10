package com.gliesestudio.phantom.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.UUID;

@Slf4j
@Service
public class ConversationContextHolder {

    private static final String CONVERSATION_ID_KEY = "conversationId";

    public void setConversationId(UUID conversationId) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.setAttribute(CONVERSATION_ID_KEY, conversationId, RequestAttributes.SCOPE_REQUEST);
            log.info("Set conversation ID {} into the request context", conversationId);
        } else {
            log.info("No request attributes in the request context. Could not set conversation ID: {}", conversationId);
        }
    }

    public UUID getConversationId() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            UUID conversationId = (UUID) requestAttributes.getAttribute(CONVERSATION_ID_KEY, RequestAttributes.SCOPE_REQUEST);
            log.info("Retried conversation ID {} from the request context", conversationId);
            return conversationId;
        } else {
            return null;
        }
    }

}
