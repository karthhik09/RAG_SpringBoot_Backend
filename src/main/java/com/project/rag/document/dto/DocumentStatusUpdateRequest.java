package com.project.rag.document.dto;

import com.project.rag.document.DocumentStatus;
import lombok.Data;

@Data
public class DocumentStatusUpdateRequest {

    private DocumentStatus status;
    private String errorMessage;
}
