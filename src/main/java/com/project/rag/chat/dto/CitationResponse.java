package com.project.rag.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitationResponse {

    private Integer citationIndex;
    private String fileName;
    private Integer pageNumber;
    private String text;
}
