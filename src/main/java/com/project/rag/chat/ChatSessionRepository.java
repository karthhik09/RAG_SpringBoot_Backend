package com.project.rag.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Integer> {
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long id);
}
