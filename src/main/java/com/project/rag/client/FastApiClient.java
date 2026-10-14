package com.project.rag.client;

import com.project.rag.chat.dto.AskRequest;
import com.project.rag.chat.dto.AskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class FastApiClient {

    private final RestClient fastApiClient;

    public AskResponse askQuestion(AskRequest request) {
        try {
            AskResponse response = fastApiClient.post()
                    .uri("/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AskResponse.class);

            if (response == null || response.getAnswer() == null) {
                throw new FastApiClientException("Received empty response from FastAPI worker.");
            }
            return response;
        } catch (Exception e) {
            throw new FastApiClientException("Failed to communicate with AI worker: " + e.getMessage(), e);
        }
    }
}
