package com.project.rag.document;

import com.project.rag.document.dto.DocumentResponse;
import com.project.rag.storage.MinIOService;
import com.project.rag.user.User;
import com.project.rag.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final MinIOService minIOService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DocumentResponse uploadDocument(MultipartFile file, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String minIoKey = minIOService.uploadFile(file);

        Document document = Document.builder()
                .userId(user.getId())
                .fileName(file.getOriginalFilename())
                .minIoKey(minIoKey)
                .status(DocumentStatus.INDEXED) // hard coded for testing
                .build();

        documentRepository.save(document);

        return DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .status(document.getStatus().name())
                .uploadedAt(document.getUploadedAt().toString())
                .build();
    }
}
