package com.gliesestudio.phantom.controller;

import com.gliesestudio.phantom.model.LlmTokenUsage;
import com.gliesestudio.phantom.service.TokenTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics/tokens")
@RequiredArgsConstructor
public class TokenAnalyticsController {

    private final TokenTrackingService tokenTrackingService;

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<LlmTokenUsage>> getConversationTokens(
            @PathVariable UUID conversationId
    ) {
        List<LlmTokenUsage> tokenUsage = tokenTrackingService.getTokenUsage(conversationId);
        return ResponseEntity.ok(tokenUsage);
    }

    @GetMapping("/usage")
    public ResponseEntity<List<LlmTokenUsage>> getTokenUsage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<LlmTokenUsage> tokenUsages = tokenTrackingService.getTokenUsage(startDate, endDate);
        return ResponseEntity.ok(tokenUsages);
    }

}
