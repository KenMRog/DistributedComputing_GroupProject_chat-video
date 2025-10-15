package com.screenshare.model;

public class HelloWorldEvent {
    private String eventType;
    private String subject;
    private String data;
    private String eventTime;
    private String id;
    
    public HelloWorldEvent() {
    }
    
    public HelloWorldEvent(String eventType, String subject, String data) {
        this.eventType = eventType;
        this.subject = subject;
        this.data = data;
        this.eventTime = java.time.Instant.now().toString();
        this.id = java.util.UUID.randomUUID().toString();
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getEventTime() {
        return eventTime;
    }
    
    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return "HelloWorldEvent{" +
                "eventType='" + eventType + '\'' +
                ", subject='" + subject + '\'' +
                ", data='" + data + '\'' +
                ", eventTime='" + eventTime + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
