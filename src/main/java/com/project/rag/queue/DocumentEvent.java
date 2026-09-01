package com.project.rag.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEvent {

    private Long documentId;
    private Long userId;
    private String fileName;
    private String minIoKey;
}
