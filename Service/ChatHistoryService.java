//package com.example.Service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//@Service
//public class ChatHistoryService {
//
//    private final StringRedisTemplate redisTemplate;
//    private final ObjectMapper objectMapper;
//
//    public ChatHistoryService(StringRedisTemplate redisTemplate) {
//        this.redisTemplate = redisTemplate;
//        this.objectMapper = new ObjectMapper();
//    }
//
//    /**
//     * 获取整个对话历史
//     * @param sessionId 会话ID
//     * @return 消息列表
//     */
//    public List<Map<String, Object>> getMessages(String sessionId) {
//        String key = "chat:history:" + sessionId;
//        List<String> rawMessages = redisTemplate.opsForList().range(key, 0, -1);
//        if (rawMessages == null || rawMessages.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        return rawMessages.stream()
//                .map(msg -> {
//                    try {
//                        return objectMapper.readValue(msg, new TypeReference<Map<String, Object>>() {});
//                    } catch (JsonProcessingException e) {
//                        return null;
//                    }
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * 保存消息到 Redis
//     * @param sessionId 会话ID
//     * @param message 消息 Map 对象
//     */
//    public void saveMessage(String sessionId, Map<String, Object> message) {
//        try {
//            String key = "chat:history:" + sessionId;
//            String jsonMessage = objectMapper.writeValueAsString(message);
//            redisTemplate.opsForList().rightPush(key, jsonMessage);
//        } catch (JsonProcessingException e) {
//            // 这里可以添加日志记录
//        }
//    }
//
//    /**
//     * 编辑指定的消息
//     * @param sessionId 会话ID
//     * @param messageId 消息唯一ID
//     * @param newContent 新的消息内容
//     * @return 成功或失败
//     */
//    public boolean editMessage(String sessionId, String messageId, String newContent) {
//        String key = "chat:history:" + sessionId;
//        List<String> rawMessages = redisTemplate.opsForList().range(key, 0, -1);
//
//        if (rawMessages == null) {
//            return false;
//        }
//
//        for (int i = 0; i < rawMessages.size(); i++) {
//            try {
//                Map<String, Object> messageMap = objectMapper.readValue(rawMessages.get(i), new TypeReference<Map<String, Object>>() {});
//
//                // 通过唯一ID找到需要修改的消息
//                if (messageMap.containsKey("id") && messageMap.get("id").toString().equals(messageId)) {
//                    messageMap.put("content", newContent); // 修改内容
//                    String updatedJson = objectMapper.writeValueAsString(messageMap);
//                    redisTemplate.opsForList().set(key, i, updatedJson); // 将修改后的消息写回 Redis
//                    return true;
//                }
//            } catch (JsonProcessingException e) {
//                // 跳过无法解析的 JSON
//            }
//        }
//        return false;
//    }
//
//    public void clearHistory(String sessionId) {
//        String key = "chat:history:" + sessionId;
//        redisTemplate.delete(key);
//    }
//
//    public boolean deleteMessage(String sessionId, String messageId) {
//        String key = "chat:history:" + sessionId;
//        List<String> rawMessages = redisTemplate.opsForList().range(key, 0, -1);
//
//        if (rawMessages == null || rawMessages.isEmpty()) {
//            return false;
//        }
//
//        try {
//            List<Map<String, Object>> messages = rawMessages.stream()
//                    .map(msg -> {
//                        try {
//                            return objectMapper.readValue(msg, new TypeReference<Map<String, Object>>() {});
//                        } catch (JsonProcessingException e) {
//                            return null;
//                        }
//                    })
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            int indexToRemove = -1;
//            for (int i = 0; i < messages.size(); i++) {
//                if (Objects.equals(messages.get(i).get("id"), messageId)) {
//                    indexToRemove = i;
//                    break;
//                }
//            }
//
//            if (indexToRemove != -1) {
//                // 检查是否为用户消息，如果是，则删除其后续的 AI 消息
//                boolean isUserMessage = Objects.equals(messages.get(indexToRemove).get("sender"), "user");
//
//                // 删除原消息
//                messages.remove(indexToRemove);
//
//                // 如果是用户消息且后面有 AI 消息，则删除 AI 消息
//                if (isUserMessage && indexToRemove < messages.size() && Objects.equals(messages.get(indexToRemove).get("sender"), "ai")) {
//                    messages.remove(indexToRemove);
//                }
//
//                // 清空 Redis 列表并写入新列表
//                redisTemplate.delete(key);
//                List<String> updatedRawMessages = messages.stream()
//                        .map(msg -> {
//                            try {
//                                return objectMapper.writeValueAsString(msg);
//                            } catch (JsonProcessingException e) {
//                                return null;
//                            }
//                        })
//                        .filter(Objects::nonNull)
//                        .collect(Collectors.toList());
//
//                if (!updatedRawMessages.isEmpty()) {
//                    redisTemplate.opsForList().rightPushAll(key, updatedRawMessages);
//                }
//                return true;
//            }
//        } catch (Exception e) {
//            // 捕获所有潜在的解析或操作错误
//        }
//        return false;
//    }
//
//    public void deleteSession(String sessionId) {
//        String Key =  "chat:history:" + sessionId;
//        redisTemplate.delete(Key);
//    }
//}
// src/main/java/com/example/Service/ChatHistoryService.java
//package com.example.Service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class ChatHistoryService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ChatHistoryService.class);
//    private final StringRedisTemplate redisTemplate;
//    private final ObjectMapper objectMapper;
//
//    public ChatHistoryService(StringRedisTemplate redisTemplate) {
//        this.redisTemplate = redisTemplate;
//        this.objectMapper = new ObjectMapper();
//    }
//
//    /**
//     * 获取整个对话历史
//     * @param sessionId 会话ID
//     * @return 消息列表
//     */
//    public List<Map<String, Object>> getMessages(String sessionId) {
//        String key = "chat:history:" + sessionId;
//        List<String> rawMessages = redisTemplate.opsForList().range(key, 0, -1);
//        if (rawMessages == null || rawMessages.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        return rawMessages.stream()
//                .map(msg -> {
//                    try {
//                        return objectMapper.readValue(msg, new TypeReference<Map<String, Object>>() {});
//                    } catch (JsonProcessingException e) {
//                        logger.error("Error parsing chat message from Redis: {}", msg, e);
//                        return null;
//                    }
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * 【改动点1】简化并重载 saveMessage 方法，直接接收角色和内容
//     * 保存消息到 Redis
//     * @param sessionId 会话ID
//     * @param role 角色（user 或 assistant）
//     * @param content 消息内容
//     */
//    public void saveMessage(String sessionId, String role, String content) {
//        try {
//            String key = "chat:history:" + sessionId;
//            Map<String, Object> message = new HashMap<>();
//            message.put("id", UUID.randomUUID().toString()); // 生成唯一ID
//            message.put("role", role);
//            message.put("content", content);
//            message.put("timestamp", System.currentTimeMillis()); // 使用时间戳
//            String jsonMessage = objectMapper.writeValueAsString(message);
//            redisTemplate.opsForList().rightPush(key, jsonMessage);
//        } catch (JsonProcessingException e) {
//            logger.error("Error saving chat message to Redis: {}", content, e);
//        }
//    }
//
//    /**
//     * 【保留原版】保留原版 saveMessage 方法，以防其他地方使用
//     * @param sessionId
//     * @param message
//     */
//    public void saveMessage(String sessionId, Map<String, Object> message) {
//        try {
//            String key = "chat:history:" + sessionId;
//            String jsonMessage = objectMapper.writeValueAsString(message);
//            redisTemplate.opsForList().rightPush(key, jsonMessage);
//        } catch (JsonProcessingException e) {
//            logger.error("Error saving chat message to Redis: {}", message, e);
//        }
//    }
//
//    /**
//     * 编辑指定的消息
//     * @param sessionId 会话ID
//     * @param messageId 消息唯一ID
//     * @param newContent 新的消息内容
//     * @return 成功或失败
//     */
//    public boolean editMessage(String sessionId, String messageId, String newContent) {
//        String key = "chat:history:" + sessionId;
//        List<String> rawMessages = redisTemplate.opsForList().range(key, 0, -1);
//
//        if (rawMessages == null || rawMessages.isEmpty()) {
//            return false;
//        }
//
//        for (int i = 0; i < rawMessages.size(); i++) {
//            try {
//                Map<String, Object> messageMap = objectMapper.readValue(rawMessages.get(i), new TypeReference<Map<String, Object>>() {});
//                if (messageMap.containsKey("id") && messageMap.get("id").toString().equals(messageId)) {
//                    messageMap.put("content", newContent);
//                    String updatedJson = objectMapper.writeValueAsString(messageMap);
//                    redisTemplate.opsForList().set(key, i, updatedJson);
//                    return true;
//                }
//            } catch (JsonProcessingException e) {
//                logger.error("Error parsing message during edit: {}", rawMessages.get(i), e);
//            }
//        }
//        return false;
//    }
//
//    public void clearHistory(String sessionId) {
//        String key = "chat:history:" + sessionId;
//        redisTemplate.delete(key);
//        logger.info("Chat history cleared for session: {}", sessionId);
//    }
//
//    /**
//     * 【改动点2】更新 deleteMessage 方法逻辑，使其更健壮
//     * @param sessionId
//     * @param messageId
//     * @return
//     */
//    public boolean deleteMessage(String sessionId, String messageId) {
//        String key = "chat:history:" + sessionId;
//        List<String> rawMessages = redisTemplate.opsForList().range(key, 0, -1);
//
//        if (rawMessages == null || rawMessages.isEmpty()) {
//            return false;
//        }
//
//        List<Map<String, Object>> messages = rawMessages.stream()
//                .map(msg -> {
//                    try {
//                        return objectMapper.readValue(msg, new TypeReference<Map<String, Object>>() {});
//                    } catch (JsonProcessingException e) {
//                        logger.error("Error parsing message during deletion: {}", msg, e);
//                        return null;
//                    }
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//        int indexToRemove = -1;
//        for (int i = 0; i < messages.size(); i++) {
//            if (Objects.equals(messages.get(i).get("id"), messageId)) {
//                indexToRemove = i;
//                break;
//            }
//        }
//
//        if (indexToRemove != -1) {
//            // 删除原消息
//            messages.remove(indexToRemove);
//
//            // 如果删除的是用户消息，则检查并删除紧随其后的AI消息
//            if (indexToRemove < messages.size() && Objects.equals(messages.get(indexToRemove).get("role"), "assistant")) {
//                messages.remove(indexToRemove);
//            }
//
//            // 清空 Redis 列表并写入新列表
//            redisTemplate.delete(key);
//            List<String> updatedRawMessages = messages.stream()
//                    .map(msg -> {
//                        try {
//                            return objectMapper.writeValueAsString(msg);
//                        } catch (JsonProcessingException e) {
//                            logger.error("Error writing updated message to JSON: {}", msg, e);
//                            return null;
//                        }
//                    })
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            if (!updatedRawMessages.isEmpty()) {
//                redisTemplate.opsForList().rightPushAll(key, updatedRawMessages);
//            }
//            logger.info("Message(s) deleted from session {}. Message ID: {}", sessionId, messageId);
//            return true;
//        }
//
//        return false;
//    }
//
//    public void deleteSession(String sessionId) {
//        String Key =  "chat:history:" + sessionId;
//        redisTemplate.delete(Key);
//        logger.info("Session and history deleted for session: {}", sessionId);
//    }
//}

package com.example.Service;

import com.example.model.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ChatHistoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final long SESSION_TIMEOUT_MINUTES = 30;

    public ChatHistoryService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getSessionKey(String sessionId) {
        return "chat:session:" + sessionId;
    }

    /**
     * 获取指定会话ID的所有聊天记录
     * @param sessionId 会话ID
     * @return 聊天记录列表
     */
    public List<Message> getMessages(String sessionId) {
        String sessionKey = getSessionKey(sessionId);
        List<Object> rawMessages = redisTemplate.opsForList().range(sessionKey, 0, -1);
        if (rawMessages == null) {
            return new java.util.ArrayList<>();
        }
        return rawMessages.stream()
                .map(obj -> (Message) obj)
                .collect(Collectors.toList());
    }

    /**
     * 将一条消息保存到指定会话的历史记录中
     * @param sessionId 会话ID
     * @param message 要保存的消息
     */
    public void saveMessage(String sessionId, Message message) {
        String sessionKey = getSessionKey(sessionId);
        redisTemplate.opsForList().rightPush(sessionKey, message);
        // 设定会话超时时间为30分钟
        long SESSION_TIMEOUT_MINUTES = 30;
        redisTemplate.expire(sessionKey, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES); // 更新超时时间
    }

    /**
     * 删除指定会话ID的所有聊天记录
     * @param sessionId 会话ID
     */
    public void deleteSession(String sessionId) {
        String sessionKey = getSessionKey(sessionId);
        redisTemplate.delete(sessionKey);
    }

    /**
     * 根据消息ID删除指定会话中的某条消息
     * @param sessionId 会话ID
     * @param messageId 消息ID
     */
    public void deleteMessage(String sessionId, String messageId) {
        String sessionKey = getSessionKey(sessionId);
        List<Message> messages = getMessages(sessionId);
        messages.removeIf(m -> m.getId().equals(messageId));

        // 清除旧列表并保存新列表
        redisTemplate.delete(sessionKey);
        if (!messages.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(sessionKey, messages.toArray());
        }
    }
}