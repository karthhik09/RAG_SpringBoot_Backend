package com.project.rag.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.rag.document.dto.DocumentResponse;
import com.project.rag.document.dto.DocumentStatusUpdateRequest;
import com.project.rag.queue.DocumentEvent;
import com.project.rag.queue.DocumentEventProducer;
import com.project.rag.storage.MinIOService;
import com.project.rag.user.User;
import com.project.rag.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final MinIOService minIOService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentEventProducer eventProducer;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentResponse uploadDocument(MultipartFile file, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String minIoKey = minIOService.uploadFile(file);

        Document document = Document.builder()
                .userId(Math.toIntExact(user.getId()))
                .fileName(file.getOriginalFilename())
                .minIoKey(minIoKey)
                .status(DocumentStatus.PENDING)
                .build();

        document = documentRepository.save(document);

        DocumentEvent event = DocumentEvent.builder()
                .documentId(document.getId())
                .userId(user.getId())
                .fileName(document.getFileName())
                .minIoKey(minIoKey)
                .build();

        eventProducer.publishEvent(event);

        return DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .status(document.getStatus().name())
                .uploadedAt(document.getUploadedAt().toString())
                .build();
    }

    public void updateDocumentStatus(Long documentId, DocumentStatusUpdateRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setStatus(request.getStatus());

        if (request.getErrorMessage() != null) {
            document.setErrorMessage(request.getErrorMessage());
        }

        documentRepository.save(document);
    }

    public void deleteDocument(Long documentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Document document = documentRepository.findByIdAndUserId(documentId, user.getId())
                .orElseThrow(() -> new RuntimeException("Document not found or unauthorized"));

        minIOService.deleteFile(document.getMinIoKey());

        documentRepository.delete(document);

        try {
            String jsonMessage = objectMapper.writeValueAsString(
                    java.util.Map.of(
                            "documentId", documentId,
                            "userId", user.getId(),
                            "action", "DELETE"
                    )
            );
            stringRedisTemplate.opsForList().leftPush("document_deletion_queue", jsonMessage);
        } catch (Exception e) {
            System.err.println("Failed to notify AI worker of document deletion: " + e.getMessage());
        }
    }
}
