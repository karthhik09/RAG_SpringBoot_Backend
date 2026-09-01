package com.project.rag.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String fileName;
    private String status;
    private String uploadedAt;
}
