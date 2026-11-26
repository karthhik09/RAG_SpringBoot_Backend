package com.project.rag.user;

import com.project.rag.chat.ChatMessageRepository;
import com.project.rag.chat.ChatSession;
import com.project.rag.chat.ChatSessionRepository;
import com.project.rag.client.FastApiClient;
import com.project.rag.document.Document;
import com.project.rag.document.DocumentRepository;
import com.project.rag.exception.ResourceNotFoundException;
import com.project.rag.exception.UnauthorizedAccessException;
import com.project.rag.storage.MinIOService;
import com.project.rag.user.dto.ChangePasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final DocumentRepository documentRepository;
    private final MinIOService minIOService;
    private final FastApiClient fastApiClient;

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedAccessException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<ChatSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (ChatSession session : sessions) {
            messageRepository.deleteAllBySessionId(session.getId());
            sessionRepository.delete(session);
        }

        List<Document> documents = documentRepository.findByUserId(user.getId());
        for (Document doc : documents) {
            try {
                fastApiClient.deleteDocumentVectors(doc.getId());
            } catch (Exception e) {
                System.err.println("Warning: Failed to delete vectors for document ID " + doc.getId() + " - " + e.getMessage());
            }

            try {
                minIOService.deleteFile(doc.getFileName());
            } catch (Exception e) {
                System.err.println("Warning: Failed to delete file from MinIO: " + doc.getFileName() + " - " + e.getMessage());
            }
        }
        documentRepository.deleteAll(documents);

        userRepository.delete(user);
    }
}