package com.gliesestudio.phantom.repository;

import com.gliesestudio.phantom.model.LlmTokenUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LlmTokenUsageRepository extends JpaRepository<LlmTokenUsage, UUID> {

    List<LlmTokenUsage> findByConversationId(UUID conversationId);

    List<LlmTokenUsage> findByTimestampBetween(LocalDateTime timestampAfter, LocalDateTime timestampBefore);

}
