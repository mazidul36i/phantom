package com.gliesestudio.phantom.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "llm_token_usage")
@EntityListeners(AuditingEntityListener.class)
public class LlmTokenUsage implements Serializable {

    @Serial
    private static final long serialVersionUID = -8829005377994067781L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "provider")
    private String provider;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "user_message_length")
    private Integer userMessageLength;

    @Column(name = "response_length")
    private Integer responseLength;

    @CreatedDate
    private LocalDateTime timestamp;

}
