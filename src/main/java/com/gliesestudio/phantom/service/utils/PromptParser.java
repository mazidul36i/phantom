package com.gliesestudio.phantom.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class PromptParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String parsePrompt(Map<String, Object> map) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                               .writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize user_details", e);
        }
    }

}
