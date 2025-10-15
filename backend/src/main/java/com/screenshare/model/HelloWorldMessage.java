package com.screenshare.model;

public class HelloWorldMessage {
    private String message;
    private String sender;
    private long timestamp;
    
    public HelloWorldMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public HelloWorldMessage(String message, String sender) {
        this.message = message;
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "HelloWorldMessage{" +
                "message='" + message + '\'' +
                ", sender='" + sender + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
