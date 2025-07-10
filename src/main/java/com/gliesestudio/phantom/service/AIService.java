package com.gliesestudio.phantom.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final WebClient webClient;
    private final String model;

    public AIService(WebClient ollamaWebClient,
                     @Value("${ollama.model}") String model) {
        this.webClient = ollamaWebClient;
        this.model     = model;
    }

    public Mono<String> generateCompletion(String prompt) {
        return webClient.post()
                        .uri(uriBuilder -> uriBuilder.path("/v1/models/{model}/completions").build(model))
                        .bodyValue(Map.of(
                                "prompt", prompt,
                                "max_tokens", 512,
                                "temperature", 0.7
                        ))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(body -> ((Map<?, ?>) ((java.util.List<?>) body.get("choices")).get(0)).get("text").toString());
    }

    public float[] generateEmbedding(String text) {
        try {
            Map<?, ?> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/v1/models/{model}/embeddings").build(model))
                    .bodyValue(Map.of("input", text))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response == null || !response.containsKey("data")) return null;
            List<?> data = (List<?>) response.get("data");
            if (data.isEmpty()) return null;
            List<Number> embList = ((Map<?, ?>) data.get(0)).containsKey("embedding")
                    ? (List<Number>) ((Map<?, ?>) data.get(0)).get("embedding")
                    : List.of();
            float[] emb = new float[embList.size()];
            for (int i = 0; i < embList.size(); i++) emb[i] = embList.get(i).floatValue();
            return emb;
        } catch (Exception e) {
            // Log error in real app
            return null;
        }
    }

}