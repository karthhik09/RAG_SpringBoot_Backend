package com.project.rag.chat.dto;

import lombok.Data;

@Data
public class AskResponse {

    private String answer;
    private Integer sessionId;
    // need to implement the citation part later
}
