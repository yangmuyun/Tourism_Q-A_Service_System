//package com.example.Service;
//
//import com.alibaba.dashscope.app.Application;
//import com.alibaba.dashscope.app.ApplicationParam;
//import com.alibaba.dashscope.app.ApplicationResult;
//import com.alibaba.dashscope.exception.ApiException;
//import com.alibaba.dashscope.exception.InputRequiredException;
//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.example.model.Message;
//import lombok.extern.slf4j.Slf4j;
//import com.example.model.MessageType;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//import com.alibaba.dashscope.common.History;
//import com.alibaba.dashscope.common.Message.Role;
//@Service
//@Slf4j
//public class BailianService {
//
//    @Value("${spring.ai.dashscope.api-key}")
//    private String apiKey;
//
//    @Value("${aliyun.bailian.app-id}")
//    private String appId;
//
//    private final Map<String, List<Message>> chatSessions = new ConcurrentHashMap<>();
//
//    public String call(String sessionId, String prompt) {
//
//        Message userMessage = new Message(MessageType.USER,prompt);
//        saveMessage(sessionId,userMessage);
//
//        // Retrieve chat history for multi-turn conversation
//        List<Message> sessionMessages = chatSessions.getOrDefault(sessionId, new ArrayList<>());
//
//        List<ChatHistory> history = sessionMessages.stream()
//                .filter(msg -> msg.getType() != MessageType.SYSTEM) // Exclude system messages from history
//                .map(this::toDashScopeChatHistory)
//                .collect(Collectors.toList());
//
//        try {
//            ApplicationParam param = ApplicationParam.builder()
//                    .apiKey(apiKey)
//                    .appId(appId)
//                    .prompt(prompt)
//                    .history(history)
//                    .build();
//
//            Application application = new Application();
//            ApplicationResult result = application.call(param);
//
//            if (result != null && result.getOutput() != null && result.getOutput().getText() != null) {
//
//                String aiResponse = result.getOutput().getText();
//                // Step 4: Save the AI's response to the session history.
//                Message aiMessage = new Message(MessageType.ASSISTANT, aiResponse);
//                saveMessage(sessionId, aiMessage);
//                return aiResponse;
////                return result.getOutput().getText();
//            } else {
//                log.error("DashScope API returned empty response.");
//                throw new RuntimeException("DashScope API returned empty response.");
//            }
//        } catch (NoApiKeyException | ApiException | InputRequiredException e) {
//            log.error("Error calling DashScope API: {}", e.getMessage(), e);
//            throw new RuntimeException("Error calling DashScope API", e);
//        }
//    }
//
//    private ChatHistory toDashScopeChatHistory(Message message) {
//        Role role = message.getType() == MessageType.USER ? Role.USER : Role.ASSISTANT;
//        return new ChatHistory(role.getValue(), message.getContent());
//    }
//
//    public List<Message> getChatHistory(String sessionId) {
//        return chatSessions.getOrDefault(sessionId, new ArrayList<>());
//    }
//
//    public void saveMessage(String sessionId, Message message) {
//        chatSessions.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
//    }
//
//    public void deleteSession(String sessionId) {
//        chatSessions.remove(sessionId);
//    }
//
//    public void clearSessionMessages(String sessionId) {
//        chatSessions.getOrDefault(sessionId, new ArrayList<>()).clear();
//    }
//
//    public void deleteMessage(String sessionId, String messageId) {
//        List<Message> messages = chatSessions.get(sessionId);
//        if (messages != null) {
//            messages.removeIf(m -> m.getId().equals(messageId));
//        }
//    }
//}

//package com.example.Service;
//
//import com.alibaba.cloud.ai.dashscope.common.DashScopeException;
//import com.alibaba.dashscope.app.Application;
//import com.alibaba.dashscope.app.ApplicationOutput;
//import com.alibaba.dashscope.app.ApplicationParam;
//import com.alibaba.dashscope.app.ApplicationResult;
//import com.alibaba.dashscope.common.History;
//import com.alibaba.dashscope.common.Result;
//import com.alibaba.dashscope.exception.ApiException;
//import com.alibaba.dashscope.exception.InputRequiredException;
//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.example.model.Message;
//import com.example.model.MessageType;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//public class BailianService {
//
//    @Value("${spring.ai.dashscope.api-key}")
//    private String apiKey;
//
//    @Value("${aliyun.bailian.app-id}")
//    private String appId;
//
//    private final Map<String, List<Message>> chatSessions = new ConcurrentHashMap<>();
//
//    public String call(String sessionId, String prompt) {
//
//        Message userMessage = new Message(MessageType.USER, prompt);
//        saveMessage(sessionId, userMessage);
//
//        List<Message> sessionMessages = chatSessions.getOrDefault(sessionId, new ArrayList<>());
//
//        List<History> history = sessionMessages.stream()
//                .filter(msg -> msg.getType() != MessageType.SYSTEM)
//                .map(this::toDashScopeHistory)
//                .collect(Collectors.toList());
//
//        try {
//            ApplicationParam param = ApplicationParam.builder()
//                    .apiKey(apiKey)
//                    .appId(appId)
//                    .prompt(prompt)
//                    .history(history)
//                    .build();
//
//            Application application = new Application();
//            ApplicationResult result = application.call(param);
//
//            if (result != null && result.getOutput() != null && result.getOutput().getText() != null) {
//                String aiResponse = result.getOutput().getText();
//                Message aiMessage = new Message(MessageType.ASSISTANT, aiResponse);
//                saveMessage(sessionId, aiMessage);
//                return aiResponse;
//            } else {
//                log.error("DashScope API returned empty response.");
//                throw new RuntimeException("DashScope API returned empty response.");
//            }
//        } catch (NoApiKeyException | ApiException | InputRequiredException e) {
//            log.error("Error calling DashScope API: {}", e.getMessage(), e);
//            throw new RuntimeException("Error calling DashScope API", e);
//        }
//    }
//
//    private History toDashScopeHistory(Message message) {
//        if (message.getType() == MessageType.USER) {
//            return History.builder()
//                    .user(message.getContent())
//                    .build();
//        } else {
//            return History.builder()
//                    .bot(message.getContent())
//                    .build();
//        }
//    }
//
//    public List<Message> getChatHistory(String sessionId) {
//        return chatSessions.getOrDefault(sessionId, new ArrayList<>());
//    }
//
//    public void saveMessage(String sessionId, Message message) {
//        chatSessions.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
//    }
//
//    public void deleteSession(String sessionId) {
//        chatSessions.remove(sessionId);
//    }
//
//    public void clearSessionMessages(String sessionId) {
//        chatSessions.getOrDefault(sessionId, new ArrayList<>()).clear();
//    }
//
//    public void deleteMessage(String sessionId, String messageId) {
//        List<Message> messages = chatSessions.get(sessionId);
//        if (messages != null) {
//            messages.removeIf(m -> m.getId() != null && m.getId().equals(messageId));
//        }
//    }
//    public void updateMessage(String sessionId, String messageId, String newContent) {
//        List<Message> messages = chatSessions.get(sessionId);
//        if (messages != null) {
//            for (Message message : messages) {
//                if (message.getId().equals(messageId)) {
//                    message.setContent(newContent);
//                    // 这里可以添加将更改保存到数据库的逻辑
//                    break;
//                }
//            }
//        }
//    }
//    public String regenerateText(String sessionId, String selectedText, String newPrompt) {
//        // 1. 获取当前会话的完整聊天历史
//        List<Message> sessionMessages = chatSessions.getOrDefault(sessionId, new ArrayList<>());
//
//        // 2. 将历史消息转换为百炼API所需的格式，排除系统消息
//        List<History> history = sessionMessages.stream()
//                .filter(msg -> msg.getType() != MessageType.SYSTEM)
//                .map(this::toDashScopeHistory)
//                .collect(Collectors.toList());
//
//        // 3. 构建新的Prompt，包括选中文本和新要求
//        String combinedPrompt = String.format(
//                "请根据以下内容：“%s”，并结合新要求：“%s”，重新创作一段文字。",
//                selectedText,
//                newPrompt
//        );
//
//        try {
//            // 4. 构建API请求参数，并传入历史记录
//            ApplicationParam param = ApplicationParam.builder()
//                    .apiKey(apiKey)
//                    .appId(appId)
//                    .prompt(combinedPrompt)
//                    .history(history) // 将聊天历史添加到参数中
//                    .build();
//
//            Application application = new Application();
//            ApplicationResult result = application.call(param);
//
//            if (result != null && result.getOutput() != null && result.getOutput().getText() != null) {
//                String aiResponse = result.getOutput().getText();
//
//                // 5. 更新会话中的AI响应
//                // 我们需要找到原始的AI消息并更新它，而不是简单地返回新内容
//                Message originalMessage = sessionMessages.stream()
//                        .filter(msg -> msg.getContent().contains(selectedText) && msg.getType() == MessageType.ASSISTANT)
//                        .findFirst()
//                        .orElse(null);
//
//                if (originalMessage != null) {
//                    originalMessage.setContent(aiResponse);
//                } else {
//                    log.warn("Original AI message not found to update.");
//                    // 或者在这里添加新的消息到历史记录，这取决于你的业务逻辑
//                    Message newMessage = new Message(MessageType.ASSISTANT, aiResponse);
//                    saveMessage(sessionId, newMessage);
//                }
//                return aiResponse;
//            } else {
//                log.error("DashScope API returned empty response for regeneration.");
//                throw new RuntimeException("DashScope API returned empty response.");
//            }
//        } catch (NoApiKeyException | ApiException | InputRequiredException e) {
//            log.error("Error calling DashScope API for regeneration: {}", e.getMessage(), e);
//            throw new RuntimeException("Error calling DashScope API for regeneration", e);
//        }
//    }
//
//    public void streamChat(String sessionId, String prompt, SseEmitter emitter) {
//        Message userMessage = new Message(MessageType.USER, prompt);
//        saveMessage(sessionId, userMessage);
//        List<Message> sessionMessages = chatSessions.getOrDefault(sessionId, new ArrayList<>());
//        List<History> history = sessionMessages.stream()
//                .filter(msg -> msg.getType() != MessageType.SYSTEM)
//                .map(this::toDashScopeHistory)
//                .collect(Collectors.toList());
//
//        ApplicationParam param = ApplicationParam.builder()
//                .apiKey(apiKey)
//                .appId(appId)
//                .prompt(prompt)
//                .history(history)
//                // Use enableStream(true) for streaming, if available in your SDK version
//                // Or remove it and ensure application.streamCall(param) is used
//                .enableStream(true) // <--- FIX 1: Correct method for enabling streaming
//                .build();
//
//        Application application = new Application();
//        new Thread(() -> {
//            try {
//                // <--- FIX 2: Use streamCall for streaming
//                Iterable<ApplicationResult> resultIterable = application.streamCall(param);
//                StringBuilder fullResponse = new StringBuilder();
//                for (ApplicationResult result : resultIterable) {
//                    // <--- FIX 3: Correct output type
//                    ApplicationOutput output = result.getOutput();
//                    if (output != null && output.getText() != null) { // <--- FIX 4: getText() from ApplicationOutput
//                        String delta = output.getText();
//                        emitter.send(SseEmitter.event().data(delta)); // <--- Ensure data is sent as an SSE event
//                        fullResponse.append(delta);
//                    }
//                }
//                Message aiMessage = new Message(MessageType.ASSISTANT, fullResponse.toString());
//                saveMessage(sessionId, aiMessage);
//                emitter.complete();
//            } catch (DashScopeException | IOException e) { // <--- FIX 5: Use DashScopeException for SDK errors
//                log.error("Error during streaming chat: {}", e.getMessage(), e);
//                emitter.completeWithError(e);
//            }
//        }).start();
//    }
//    public void streamRegenerate(String sessionId, String selectedText, String newPrompt, SseEmitter emitter) {
//        String combinedPrompt = String.format(
//                "请根据以下内容：“%s”，并结合新要求：“%s”，重新创作一段文字。",
//                selectedText,
//                newPrompt
//        );
//        ApplicationParam param = ApplicationParam.builder()
//                .apiKey(apiKey)
//                .appId(appId)
//                .prompt(combinedPrompt)
//                .enableStream(true) // <--- FIX 1: Correct method for enabling streaming
//                .build();
//
//        Application application = new Application();
//        new Thread(() -> {
//            try {
//                // <--- FIX 2: Use streamCall for streaming
//                Iterable<ApplicationResult> resultIterable = application.streamCall(param);
//                StringBuilder fullResponse = new StringBuilder();
//                for (ApplicationResult result : resultIterable) {
//                    // <--- FIX 3: Correct output type
//                    ApplicationOutput output = result.getOutput();
//                    if (output != null && output.getText() != null) { // <--- FIX 4: getText() from ApplicationOutput
//                        String delta = output.getText();
//                        emitter.send(SseEmitter.event().data(delta)); // <--- Ensure data is sent as an SSE event
//                        fullResponse.append(delta);
//                    }
//                }
//                // When regeneration is complete, the frontend needs the final text to update the specific part.
//                // We send the full regenerated text as the last event.
//                emitter.send(SseEmitter.event().name("REGENERATION_COMPLETE").data(fullResponse.toString()));
//                emitter.complete();
//
//            } catch (DashScopeException | IOException e) { // <--- FIX 5: Use DashScopeException for SDK errors
//                log.error("Error during streaming regeneration: {}", e.getMessage(), e);
//                emitter.completeWithError(e);
//            }
//        }).start();
//    }
//
//}
package com.example.Service;

import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.common.History;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.example.model.Message;
import com.example.model.MessageType;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BailianService {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${aliyun.bailian.app-id}")
    private String appId;

    private final Map<String, List<Message>> chatSessions = new ConcurrentHashMap<>();

    // 非流式聊天方法保持不变
    public String call(String sessionId, String prompt) {
        Message userMessage = new Message(MessageType.USER, prompt);
        saveMessage(sessionId, userMessage);
        List<Message> sessionMessages = chatSessions.getOrDefault(sessionId, new ArrayList<>());
        List<History> history = sessionMessages.stream()
                .filter(msg -> msg.getType() != MessageType.SYSTEM)
                .map(this::toDashScopeHistory)
                .collect(Collectors.toList());

        try {
            ApplicationParam param = ApplicationParam.builder()
                    .apiKey(apiKey)
                    .appId(appId)
                    .prompt(prompt)
                    .history(history)
                    .build();

            Application application = new Application();
            ApplicationResult result = application.call(param);

            if (result != null && result.getOutput() != null && result.getOutput().getText() != null) {
                String aiResponse = result.getOutput().getText();
                Message aiMessage = new Message(MessageType.ASSISTANT, aiResponse);
                saveMessage(sessionId, aiMessage);
                return aiResponse;
            } else {
                log.error("DashScope API returned empty response.");
                throw new RuntimeException("DashScope API returned empty response.");
            }
        } catch (NoApiKeyException | ApiException | InputRequiredException e) {
            log.error("Error calling DashScope API: {}", e.getMessage(), e);
            throw new RuntimeException("Error calling DashScope API", e);
        }
    }

    private History toDashScopeHistory(Message message) {
        if (message.getType() == MessageType.USER) {
            return History.builder()
                    .user(message.getContent())
                    .build();
        } else {
            return History.builder()
                    .bot(message.getContent())
                    .build();
        }
    }

    public List<Message> getChatHistory(String sessionId) {
        return chatSessions.getOrDefault(sessionId, new ArrayList<>());
    }

    public void saveMessage(String sessionId, Message message) {
        chatSessions.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
    }

    public void deleteSession(String sessionId) {
        chatSessions.remove(sessionId);
    }

    public void clearSessionMessages(String sessionId) {
        chatSessions.getOrDefault(sessionId, new ArrayList<>()).clear();
    }

    public void deleteMessage(String sessionId, String messageId) {
        List<Message> messages = chatSessions.get(sessionId);
        if (messages != null) {
            messages.removeIf(m -> m.getId() != null && m.getId().equals(messageId));
        }
    }

    public void updateMessage(String sessionId, String messageId, String newContent) {
        List<Message> messages = chatSessions.get(sessionId);
        if (messages != null) {
            for (Message message : messages) {
                if (message.getId() != null && message.getId().equals(messageId)) {
                    message.setContent(newContent);
                    break;
                }
            }
        }
    }

    // 使用 RxJava 改造的流式聊天方法
    public void streamChat(String sessionId, String prompt, SseEmitter emitter) {
        Message userMessage = new Message(MessageType.USER, prompt);
        saveMessage(sessionId, userMessage);
        List<Message> sessionMessages = chatSessions.getOrDefault(sessionId, new ArrayList<>());
        List<History> history = sessionMessages.stream()
                .filter(msg -> msg.getType() != MessageType.SYSTEM)
                .map(this::toDashScopeHistory)
                .collect(Collectors.toList());

        ApplicationParam param = ApplicationParam.builder()
                .apiKey(apiKey)
                .appId(appId)
                .prompt(prompt)
                .history(history)
                .incrementalOutput(true)
                .build();

        new Thread(() -> {
            try {
                Application application = new Application();
                Flowable<ApplicationResult> flowable = application.streamCall(param);
                StringBuilder fullResponse = new StringBuilder();

                // 使用 blockingSubscribe 处理流式响应
                flowable.blockingSubscribe(
                        // onNext - 处理每个结果
                        result -> {
                            if (result.getOutput() != null && result.getOutput().getText() != null) {
                                String text = result.getOutput().getText();

                                // 逐字发送实现打字机效果
                                for (int i = 0; i < text.length(); i++) {
                                    String character = String.valueOf(text.charAt(i));
                                    try {
                                        emitter.send(SseEmitter.event().data(character));
                                        // 添加小延迟模拟打字效果
                                        Thread.sleep(20);
                                    } catch (IOException | InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        return;
                                    }
                                }

                                fullResponse.append(text);
                            }
                        },
                        // onError - 错误处理
                        error -> {
                            log.error("Stream error: {}", error.getMessage(), error);
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("Stream error: " + error.getMessage()));
                            } catch (IOException e) {
                                log.error("Failed to send error event", e);
                            }
                            emitter.completeWithError(error);
                        },
                        // onComplete - 完成处理
                        () -> {
                            // 保存完整的响应
                            Message aiMessage = new Message(MessageType.ASSISTANT, fullResponse.toString());
                            saveMessage(sessionId, aiMessage);
                            emitter.complete();
                        }
                );

            } catch (Exception e) {
                log.error("Error initializing streaming chat: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("Initialization error: " + e.getMessage()));
                } catch (IOException ex) {
                    log.error("Failed to send error event", ex);
                }
                emitter.completeWithError(e);
            }
        }).start();
    }

    // 使用 RxJava 改造的流式重写方法
    public void streamRegenerate(String sessionId, String selectedText, String newPrompt, SseEmitter emitter) {
        String combinedPrompt = String.format(
                "请根据以下内容：「%s」，并结合新要求：「%s」，重新创作一段文字。",
                selectedText,
                newPrompt
        );

        ApplicationParam param = ApplicationParam.builder()
                .apiKey(apiKey)
                .appId(appId)
                .prompt(combinedPrompt)
                .incrementalOutput(true)
                .build();

        new Thread(() -> {
            try {
                Application application = new Application();

                // 获取 Flowable
                Flowable<ApplicationResult> flowable = application.streamCall(param);
                StringBuilder fullResponse = new StringBuilder();

                flowable.blockingSubscribe(
                        result -> {
                            if (result.getOutput() != null && result.getOutput().getText() != null) {
                                String text = result.getOutput().getText();

                                // 逐字发送
                                for (int i = 0; i < text.length(); i++) {
                                    String character = String.valueOf(text.charAt(i));
                                    try {
                                        emitter.send(SseEmitter.event().data(character));
                                        Thread.sleep(20);
                                    } catch (IOException | InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        return;
                                    }
                                }

                                fullResponse.append(text);
                            }
                        },
                        error -> {
                            log.error("Regeneration stream error: {}", error.getMessage(), error);
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("Regeneration error: " + error.getMessage()));
                            } catch (IOException e) {
                                log.error("Failed to send error event", e);
                            }
                            emitter.completeWithError(error);
                        },
                        () -> {
                            // 发送完成事件和完整文本
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("REGENERATION_COMPLETE")
                                        .data(fullResponse.toString()));
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("Failed to send completion event", e);
                            }
                        }
                );

            } catch (Exception e) {
                log.error("Error initializing streaming regeneration: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("Initialization error: " + e.getMessage()));
                } catch (IOException ex) {
                    log.error("Failed to send error event", ex);
                }
                emitter.completeWithError(e);
            }
        }).start();
    }
}