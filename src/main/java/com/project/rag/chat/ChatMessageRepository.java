package com.project.rag.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    void deleteAllBySessionId(Integer sessionId);
    List<ChatMessage> findBySessionIdOrderByIdAsc(Integer sessionId);
}
