package com.project.rag.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatSessionDto {

    private Integer id;
    private Long documentId;
    private String documentName;
    private LocalDateTime createdAt;
}