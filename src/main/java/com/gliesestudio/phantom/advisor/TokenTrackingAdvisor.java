package com.gliesestudio.phantom.advisor;

import com.gliesestudio.phantom.model.LlmTokenUsage;
import com.gliesestudio.phantom.service.ConversationContextHolder;
import com.gliesestudio.phantom.service.TokenTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenTrackingAdvisor implements BaseAdvisor {

    private final ConversationContextHolder conversationContextHolder;
    private final TokenTrackingService tokenTrackingService;

    private static final ThreadLocal<TokenTrackingContext> CONTEXT_HOLDER = new ThreadLocal<>();

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // Extract context information before the call.
        int userMessageLength = chatClientRequest.copy().prompt().getUserMessage().getText().length();

        // Store context for use in after method
        TokenTrackingContext context = new TokenTrackingContext(userMessageLength);
        CONTEXT_HOLDER.set(context);

        log.info("Token tracking - before call: conversationId: {}, user message length: {}",
                 conversationContextHolder.getConversationId(), userMessageLength);

        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // Extract and store token uses after the call
        try {
            TokenTrackingContext context = CONTEXT_HOLDER.get();
            if (context != null) {
                extractAndStoreToken(chatClientResponse, context);
            }
        } catch (Exception e) {
            log.error("Failed to track token usage", e);
        } finally {
            CONTEXT_HOLDER.remove();
        }
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private void extractAndStoreToken(ChatClientResponse chatClientResponse, TokenTrackingContext context) {
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if (chatResponse == null) {
            log.error("No chat response available for token tracking");
            return;
        }

        ChatResponseMetadata metadata = chatResponse.getMetadata();
        if (metadata == null) {
            log.error("No metadata available for token tracking");
            return;
        }

        Usage usage = metadata.getUsage();
        if (usage == null) {
            log.error("No usage information available for token tracking");
            return;
        }

        // Extract token counts
        Integer promptTokens = usage.getPromptTokens();
        Integer completionTokens = usage.getCompletionTokens();
        Integer totalTokens = usage.getTotalTokens();
        Object nativeUsage = usage.getNativeUsage();
        log.info("Extracted token usage for token tracking; prompt tokens: {}, completionTokens: {}, totalTokens: {}",
                 promptTokens, completionTokens, totalTokens);
        log.info("Native usage: {}", nativeUsage);

        // If the total token is not provided, calculate it
        if (totalTokens == null && promptTokens != null && completionTokens != null) {
            totalTokens = promptTokens + completionTokens;
        }

        // Extract model info
        String model = metadata.getModel() != null ? metadata.getModel() : "unknown";

        // Get response length
        String responseContent = "";
        if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
            responseContent = chatResponse.getResult().getOutput().getText();
        }
        int responseLength = responseContent != null ? responseContent.length() : 0;

        // Create token usage record
        LlmTokenUsage llmTokenUsage = LlmTokenUsage
                .builder()
                .conversationId(conversationContextHolder.getConversationId())
                .modelName(model)
                .provider("default")
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .userMessageLength(context.userMessageLength)
                .responseLength(responseLength)
                .build();
        tokenTrackingService.saveTokenUsage(llmTokenUsage);
        log.info("Token tracking - extract and store token usage for token tracking");
    }

    private record TokenTrackingContext(int userMessageLength) {
    }
}
