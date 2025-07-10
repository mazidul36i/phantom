package com.gliesestudio.phantom.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
public class ChatRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -5877033715400470657L;

    private String message;
    private UUID conversationId;

}
