package com.project.rag.chat;

import com.project.rag.chat.dto.AskRequest;
import com.project.rag.chat.dto.AskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@RequestBody AskRequest request, Principal principal) {
        return ResponseEntity.ok(chatService.askQuestion(principal.getName(), request));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Integer sessionId, Principal principal) {
        chatService.deleteChatSession(sessionId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
