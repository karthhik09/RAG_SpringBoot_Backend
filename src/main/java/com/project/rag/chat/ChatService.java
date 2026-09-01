package com.project.rag.chat;

import com.project.rag.chat.dto.AskRequest;
import com.project.rag.chat.dto.AskResponse;
import com.project.rag.client.FastApiClient;
import com.project.rag.user.User;
import com.project.rag.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final FastApiClient fastApiClient;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    public AskResponse askQuestion(String email, AskRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        request.setUserId(Integer.toUnsignedLong(user.getId()));

        AskResponse fastApiResponse = fastApiClient.askQuestion(request);

        ChatSession session;
        if (request.getSessionId() == null) {
            session = sessionRepository.save(ChatSession.builder().userId(user.getId()).build());
        } else {
            session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new RuntimeException("Chat session not found"));

            if (!session.getUserId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to access this chat session");
            }
        }

        ChatMessage message = ChatMessage.builder()
                .session(session)
                .userQuery(request.getQuery())
                .aiResponse(fastApiResponse.getAnswer())
                .build();
        messageRepository.save(message);

        fastApiResponse.setSessionId(session.getId());

        return fastApiResponse;
    }

    @Transactional
    public void deleteChatSession(Integer sessionId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found"));

        if (!session.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this chat session");
        }

        messageRepository.deleteAllBySessionId(sessionId);

        sessionRepository.delete(session);
    }
}
