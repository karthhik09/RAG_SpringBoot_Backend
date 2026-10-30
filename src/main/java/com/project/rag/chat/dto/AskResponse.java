package com.project.rag.chat.dto;

import lombok.Data;

import java.util.List;

@Data
public class AskResponse {

    private String answer;
    private Integer sessionId;
    private List<CitationResponse> sources;
}
