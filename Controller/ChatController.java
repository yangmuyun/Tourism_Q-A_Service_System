//package com.example.Controller;
//
//import com.example.Service.ChatHistoryService;
//import com.example.Service.OpenAiService;
//import com.example.model.ChatRequest; // 导入新创建的类
//import com.example.model.MessageEditRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/chat")
//public class ChatController {
//
//    private final DashScopeService dashScopeService;
//    private final ChatHistoryService chatHistoryService;
//
//    public ChatController(OpenAiService openAiService, ChatHistoryService chatHistoryService) {
//        this.openAiService = openAiService;
//        this.chatHistoryService = chatHistoryService;
//    }
//
//    @PostMapping("/sessions")
//    public ResponseEntity<String> createSession(){
//        String newSessionId = UUID.randomUUID().toString();
//        return ResponseEntity.ok(newSessionId);
//    }
//
//    @PostMapping("/sessions/{sessionId}/messages")
//    public ResponseEntity<String> chat(@PathVariable String sessionId, @RequestBody ChatRequest request) {
//        try {
//            String aiResponse = openAiService.chat(sessionId, request.getPrompt());
//            return ResponseEntity.ok(aiResponse);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error processing chat request: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/sessions/{sessionId}/messages")
//    public ResponseEntity<List<Map<String, Object>>> getChatHistory(@PathVariable String sessionId) {
//        try {
//            List<Map<String, Object>> messages = chatHistoryService.getMessages(sessionId);
//            return ResponseEntity.ok(messages);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
//        }
//    }
//
//    @PutMapping("/sessions/{sessionId}/messages/{messageId}")
//    public ResponseEntity<String> editMessage(@PathVariable String sessionId, @PathVariable String messageId, @RequestBody MessageEditRequest request) {
//        try {
//            boolean success = chatHistoryService.editMessage(sessionId, messageId, request.getNewContent());
//            if (success) {
//                return ResponseEntity.ok("Message updated successfully");
//            } else {
//                return ResponseEntity.badRequest().body("Message not found or update failed");
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating message: " + e.getMessage());
//        }
//    }
//    @DeleteMapping("/sessions/{sessionId}/messages/{messageId}")
//    public ResponseEntity<String> deleteMessage(@PathVariable String sessionId, @PathVariable String messageId) {
//        try {
//            boolean success = chatHistoryService.deleteMessage(sessionId, messageId);
//            if (success) {
//                return ResponseEntity.ok("Message deleted successfully");
//            } else {
//                return ResponseEntity.badRequest().body("Message not found or delete failed");
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting message: " + e.getMessage());
//        }
//    }
//
//    @DeleteMapping("/sessions/{sessionId}/messages")
//    public ResponseEntity<String> clearChatHistory(@PathVariable String sessionId) {
//        try {
//            chatHistoryService.clearHistory(sessionId);
//            return ResponseEntity.ok("History cleared successfully");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error clearing history: " + e.getMessage());
//        }
//    }
//    @DeleteMapping("/sessions/{sessionId}")
//    public ResponseEntity<String> deleteSession(@PathVariable String sessionId) {
//        try {
//            chatHistoryService.deleteSession(sessionId);
//            return ResponseEntity.ok("Session deleted successfully");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting session: " + e.getMessage());
//        }
//    }
//
//}
//
//
//
// src/main/java/com/example/Controller/ChatController.java
package com.example.Controller;

import com.example.Service.BailianService;
import com.example.model.Message;
import com.example.model.MessageType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final BailianService bailianService;

    public ChatController(BailianService bailianService) {
        this.bailianService = bailianService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<String> createSession() {
        String newSessionId = UUID.randomUUID().toString();
        // 使用正确的构造函数，只传入 type 和 content
        Message systemMessage = new Message(MessageType.SYSTEM, "会话已创建。");
        bailianService.saveMessage(newSessionId, systemMessage);
        return ResponseEntity.ok(newSessionId);
    }

//    @PostMapping("/sessions/{sessionId}/messages")
//    public ResponseEntity<String> sendMessage(@PathVariable String sessionId, @RequestBody Map<String, String> payload) {
//        String prompt = payload.get("prompt");
//        if (prompt == null || prompt.trim().isEmpty()) {
//            return ResponseEntity.badRequest().body("Prompt cannot be empty.");
//        }
//        try {
//            String aiResponse = bailianService.call(sessionId, prompt);
//            return ResponseEntity.ok(aiResponse);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error processing chat request: " + e.getMessage());
//        }
//    }
    @PostMapping("/sessions/{sessionId}/messages")
    public SseEmitter sendMessage(@PathVariable String sessionId, @RequestBody Map<String, String> payload) throws IOException {
        String prompt = payload.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            SseEmitter emitter = new SseEmitter();
            emitter.send("Prompt cannot be empty.");
            emitter.complete();
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(1800000L); // Set timeout, e.g., 30 minutes
        bailianService.streamChat(sessionId, prompt, emitter);
        return emitter;
    }


    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable String sessionId) {
        List<Message> messages = bailianService.getChatHistory(sessionId);
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        bailianService.deleteSession(sessionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Void> clearChat(@PathVariable String sessionId) {
        bailianService.clearSessionMessages(sessionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/sessions/{sessionId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String sessionId, @PathVariable String messageId) {
        bailianService.deleteMessage(sessionId, messageId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

//    @PostMapping("/sessions/{sessionId}/regenerate") // 注意：将URL改为 /sessions/{sessionId}/regenerate
//    public ResponseEntity<String> regenerate(@PathVariable String sessionId, @RequestBody Map<String, String> payload) {
//        String selectedText = payload.get("selectedText");
//        String newPrompt = payload.get("newPrompt");
//
//        if (selectedText == null || selectedText.isEmpty() || newPrompt == null || newPrompt.isEmpty()) {
//            return ResponseEntity.badRequest().body("Selected text and new prompt cannot be empty.");
//        }
//
//        try {
//            // 将 sessionId 传递给服务层
//            String regeneratedContent = bailianService.regenerateText(sessionId, selectedText, newPrompt);
//            return ResponseEntity.ok(regeneratedContent);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error regenerating text: " + e.getMessage());
//        }
//    }
    @PostMapping("/sessions/{sessionId}/regenerate")
    public SseEmitter regenerate(@PathVariable String sessionId, @RequestBody Map<String, String> payload) throws IOException {
        String selectedText = payload.get("selectedText");
        String newPrompt = payload.get("newPrompt");

        if (selectedText == null || selectedText.isEmpty() || newPrompt == null || newPrompt.isEmpty()) {
            SseEmitter emitter = new SseEmitter();
            emitter.send("Selected text and new prompt cannot be empty.");
            emitter.complete();
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(1800000L);
        bailianService.streamRegenerate(sessionId, selectedText, newPrompt, emitter);
        return emitter;
    }
    @PutMapping("/sessions/{sessionId}/messages/{messageId}")
    public ResponseEntity<Void> updateMessageContent(
            @PathVariable String sessionId,
            @PathVariable String messageId,
            @RequestBody Map<String, String> payload) {
        String newContent = payload.get("newContent");
        if (newContent == null) {
            return ResponseEntity.badRequest().build();
        }
        bailianService.updateMessage(sessionId, messageId, newContent);
        return ResponseEntity.noContent().build();
    }

}