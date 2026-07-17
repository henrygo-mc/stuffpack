package com.henrygo.stuffpack.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MessageData {
    
    private String id;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime time;
    private boolean read;
    private boolean isServerMessage;
    
    public MessageData(String sender, String receiver, String content, boolean isServerMessage) {
        this.id = UUID.randomUUID().toString();
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.time = LocalDateTime.now();
        this.read = false;
        this.isServerMessage = isServerMessage;
    }
    
    public String getId() {
        return id;
    }
    
    public String getSender() {
        return sender;
    }
    
    public String getReceiver() {
        return receiver;
    }
    
    public String getContent() {
        return content;
    }
    
    public LocalDateTime getTime() {
        return time;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public boolean isServerMessage() {
        return isServerMessage;
    }
    
    public String getFormattedTime() {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}