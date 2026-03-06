package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();
    private String content;
    private MessageType type;

    // 为方便调用而添加的构造函数
    public Message(MessageType type, String content) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.content = content;
    }
}