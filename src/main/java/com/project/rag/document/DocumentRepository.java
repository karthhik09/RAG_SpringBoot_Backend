package com.project.rag.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {

    List<Document> findAllByUserId(Long userId);
    Optional<Document> findByIdAndUserId(Long id, Long userId);
}
