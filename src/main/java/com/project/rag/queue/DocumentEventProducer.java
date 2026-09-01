package com.project.rag.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class DocumentEventProducer {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_NAME = "document_ingestion_queue";

    public void publishEvent(DocumentEvent event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            stringRedisTemplate.opsForList().leftPush(QUEUE_NAME, jsonMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialise event for Redis", e);
        }
    }
}
