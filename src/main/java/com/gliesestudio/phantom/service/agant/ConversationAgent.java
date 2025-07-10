package com.gliesestudio.phantom.service.agant;

import com.gliesestudio.phantom.advisor.TokenTrackingAdvisor;
import com.gliesestudio.phantom.service.ConversationContextHolder;
import com.gliesestudio.phantom.service.utils.PromptParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ConversationAgent implements PhantomAgent {

    private final ConversationContextHolder conversationContextHolder;
    private final ChatClient chatClient;

    public ConversationAgent(
            ConversationContextHolder conversationContextHolder,
            ChatModel chatModel,
            ChatMemory chatMemory,
            TokenTrackingAdvisor tokenTrackingAdvisor,

            @Value("classpath:/prompts/conversation-agent-prompt.st") Resource conversationAgentPrompt
    ) {
        this.conversationContextHolder = conversationContextHolder;
        this.chatClient                = ChatClient.builder(chatModel)
                                                   .defaultSystem(conversationAgentPrompt)
                                                   .defaultTemplateRenderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                                                   .defaultAdvisors(
                                                           new SimpleLoggerAdvisor(),
                                                           MessageChatMemoryAdvisor.builder(chatMemory).build(),
                                                           tokenTrackingAdvisor
                                                   )
                                                   //                                           .defaultTools(...)
                                                   .build();
    }

    @Override
    public Map<String, ?> runWithContext(String userMessage, Map<String, Object> context) {
        log.info("Running conversation agent with input: {}", userMessage);

        // Set the conversation ID in the context
        UUID conversationId = conversationContextHolder.getConversationId();
        context.put("conversationId", conversationId);

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("name", "Mazidul Islam");
        userDetails.put("age", "22");
        userDetails.put("location", "Assam, India");
        String parsedUserDetails = PromptParser.parsePrompt(userDetails);

        Map<String, Object> systemParams = Map.of(
                "user_details", parsedUserDetails
        );

        // Execute the chat client with the input and context
        var chatResponse = chatClient.prompt()
                                     .system(s -> s.params(systemParams))
                                     .user(userMessage)
                                     .advisors(as -> as.param(ChatMemory.CONVERSATION_ID, conversationId))
                                     .call()
                                     .entity(new MapOutputConverter());

        log.info("Conversation response: {}", chatResponse);
        return chatResponse;
    }

}
