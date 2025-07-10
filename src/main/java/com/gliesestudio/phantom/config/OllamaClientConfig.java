package com.gliesestudio.phantom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OllamaClientConfig {

    @Value("${ollama.host}")
    private String host;

    @Value("${ollama.port}")
    private int port;

    @Bean
    public WebClient ollamaWebClient() {
        String baseUrl = String.format("http://%s:%d", host, port);
        return WebClient.builder()
                        .baseUrl(baseUrl)
                        .build();
    }
}