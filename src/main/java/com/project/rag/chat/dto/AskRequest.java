package com.project.rag.chat.dto;

import lombok.Data;

@Data
public class AskRequest {

    private String query;
    private Long userId;
    private Integer sessionId;
}
