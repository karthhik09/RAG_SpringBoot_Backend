package com.project.rag.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChatMessageDto {

    private Integer id;
    private String userQuery;
    private String aiResponse;
    private LocalDateTime createdAt;
    private List<CitationResponse> sources;
}
