package com.gliesestudio.phantom.service;

import com.gliesestudio.phantom.model.LlmTokenUsage;
import com.gliesestudio.phantom.repository.LlmTokenUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenTrackingService {

    private final LlmTokenUsageRepository llmTokenUsageRepository;

    public void saveTokenUsage(LlmTokenUsage llmTokenUsage) {
        llmTokenUsageRepository.save(llmTokenUsage);
    }

    public List<LlmTokenUsage> getTokenUsage(UUID conversationId) {
        log.info("Getting LlmTokenUsage by conversationId {}", conversationId);
        return llmTokenUsageRepository.findByConversationId(conversationId);
    }

    public List<LlmTokenUsage> getTokenUsage(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1L).atStartOfDay();

        return llmTokenUsageRepository.findByTimestampBetween(startDateTime, endDateTime);
    }
}
