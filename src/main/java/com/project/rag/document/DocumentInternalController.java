package com.project.rag.document;

import com.project.rag.document.dto.DocumentStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/documents")
@RequiredArgsConstructor
public class DocumentInternalController {

    private final DocumentService documentService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody DocumentStatusUpdateRequest request) {
        documentService.updateDocumentStatus(id, request);
        return ResponseEntity.ok().build();
    }
}
