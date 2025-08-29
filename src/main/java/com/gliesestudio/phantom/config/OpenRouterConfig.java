package com.gliesestudio.phantom.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OpenRouterConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.model}")
    String model;

    @Bean
    @Primary
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .build();
    }

    @Bean
    @Primary
    public OpenAiChatModel openAiChatClient(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                              .openAiApi(openAiApi)
                              .defaultOptions(OpenAiChatOptions.builder()
                                                               .model(model)
                                                               .build()
                              )
                              .build();
    }
}
