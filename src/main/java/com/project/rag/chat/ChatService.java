package com.project.rag.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.rag.chat.dto.*;
import com.project.rag.client.FastApiClient;
import com.project.rag.document.Document;
import com.project.rag.document.DocumentRepository;
import com.project.rag.user.User;
import com.project.rag.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final FastApiClient fastApiClient;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AskResponse askQuestion(String email, AskRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        request.setUserId(user.getId());

        ChatSession session;
        if (request.getSessionId() == null) {
            if (request.getDocumentId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "documentId is required to start a new chat session.");
            }

            Document doc = documentRepository.findByIdAndUserId(request.getDocumentId(), user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found or unauthorized"));

            session = sessionRepository.save(ChatSession.builder()
                    .userId(user.getId())
                    .documentId(doc.getId())
                    .build());
        } else {
            session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat session not found"));

            if (!session.getUserId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to access this chat session");
            }
            request.setDocumentId(session.getDocumentId());
        }

        request.setSessionId(session.getId());

        AskResponse fastApiResponse = fastApiClient.askQuestion(request);

        String sourcesJson = null;
        try {
            if (fastApiResponse.getSources() != null && !fastApiResponse.getSources().isEmpty()) {
                sourcesJson = objectMapper.writeValueAsString(fastApiResponse.getSources());
            }
        } catch (Exception e) {
            System.err.println("Failed to serialize sources to JSON: " + e.getMessage());
        }

        ChatMessage message = ChatMessage.builder()
                .session(session)
                .userQuery(request.getQuery())
                .aiResponse(fastApiResponse.getAnswer())
                .sourcesJson(sourcesJson)
                .build();
        messageRepository.save(message);

        fastApiResponse.setSessionId(session.getId());

        return fastApiResponse;
    }

    @Transactional
    public void deleteChatSession(Integer sessionId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat session not found"));

        if (!session.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this chat session");
        }

        messageRepository.deleteAllBySessionId(sessionId);
        sessionRepository.delete(session);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionDto> getUserSessions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<ChatSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return sessions.stream().map(session -> {
            String docName = documentRepository.findById(session.getDocumentId())
                    .map(Document::getFileName)
                    .orElse("Unknown Document");

            return ChatSessionDto.builder()
                    .id(session.getId())
                    .documentId(session.getDocumentId())
                    .documentName(docName)
                    .createdAt(session.getCreatedAt())
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getSessionMessages(Integer sessionId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByIdAsc(sessionId);

        return messages.stream().map(msg -> {
            List<CitationResponse> sources = null;
            if (msg.getSourcesJson() != null && !msg.getSourcesJson().isEmpty()) {
                try {
                    sources = objectMapper.readValue(msg.getSourcesJson(), new TypeReference<List<CitationResponse>>() {});
                } catch (Exception e) {
                    System.err.println("Failed to parse sources JSON for message ID: " + msg.getId());
                }
            }

            return ChatMessageDto.builder()
                    .id(msg.getId())
                    .userQuery(msg.getUserQuery())
                    .aiResponse(msg.getAiResponse())
                    .createdAt(msg.getCreatedAt())
                    .sources(sources)
                    .build();
        }).toList();
    }
}
