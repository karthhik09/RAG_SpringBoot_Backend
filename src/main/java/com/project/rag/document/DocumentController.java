package com.project.rag.document;

import com.project.rag.document.dto.DocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadFile(@RequestParam("file") MultipartFile file, Principal principal) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(file, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, Principal principal) {
        documentService.deleteDocument(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
