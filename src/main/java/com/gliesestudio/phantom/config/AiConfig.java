package com.gliesestudio.phantom.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;


@Slf4j
@Configuration
public class AiConfig {

    @Value("${spring.ai.provider:ollama}")
    private String aiProvider;

    @Bean
    public ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
        log.info("Initializing ToolExecutionExceptionProcessor");
        return new DefaultToolExecutionExceptionProcessor(true) {
            @Override
            public String process(ToolExecutionException exception) {
                log.error("Tool execution exception", exception);
                return super.process(exception);
            }
        };
    }

    @Bean
    public ChatMemoryRepository chatMemoryRepository(JdbcTemplate jdbcTemplate) {
        log.info("Initializing ChatMemoryRepository");
        assert jdbcTemplate.getDataSource() != null;
        return JdbcChatMemoryRepository.builder()
                                       .jdbcTemplate(jdbcTemplate)
                                       .dialect(JdbcChatMemoryRepositoryDialect.from(jdbcTemplate.getDataSource()))
                                       .build();
    }

    @Bean
    public ChatModel chatModel(OllamaChatModel ollamaChatModel) {
        log.info("Initializing ChatModel with provider: {}", aiProvider);

        return switch (aiProvider) {
            case "ollama" -> {
                log.info("Using OllamaChatModel as the AI provider");
                yield ollamaChatModel;
            }
            case null, default -> {
                log.warn("No AI provider specified, defaulting to OllamaChatModel");
                yield ollamaChatModel;
            }
        };
    }

}
