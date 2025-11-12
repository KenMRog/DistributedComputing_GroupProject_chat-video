package com.screenshare.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Notification message for screen share notifications sent via Azure Service Bus
 * Used for real-time notifications to users about screen sharing activity
 */
public class ScreenShareNotificationMessage {
    
    @JsonProperty("notificationId")
    private String notificationId;
    
    @JsonProperty("notificationType")
    private String notificationType; // SESSION_STARTED, SESSION_ENDED, PARTICIPANT_JOINED, etc.
    
    @JsonProperty("recipientUserId")
    private Long recipientUserId;
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("hostUserId")
    private Long hostUserId;
    
    @JsonProperty("hostUsername")
    private String hostUsername;
    
    @JsonProperty("roomId")
    private Long roomId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("body")
    private String body;
    
    @JsonProperty("sessionStatus")
    private String sessionStatus;
    
    @JsonProperty("participantCount")
    private Integer participantCount;
    
    @JsonProperty("priority")
    private String priority; // HIGH, NORMAL, LOW
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    // Constructors
    public ScreenShareNotificationMessage() {
        this.timestamp = Instant.now().toString();
        this.priority = "NORMAL";
    }
    
    public ScreenShareNotificationMessage(String notificationType, Long recipientUserId) {
        this();
        this.notificationType = notificationType;
        this.recipientUserId = recipientUserId;
        this.notificationId = java.util.UUID.randomUUID().toString();
    }
    
    // Notification type constants
    public static final String TYPE_SESSION_STARTED = "SESSION_STARTED";
    public static final String TYPE_SESSION_ENDED = "SESSION_ENDED";
    public static final String TYPE_PARTICIPANT_JOINED = "PARTICIPANT_JOINED";
    public static final String TYPE_PARTICIPANT_LEFT = "PARTICIPANT_LEFT";
    public static final String TYPE_SESSION_PAUSED = "SESSION_PAUSED";
    public static final String TYPE_SESSION_RESUMED = "SESSION_RESUMED";
    public static final String TYPE_QUALITY_CHANGED = "QUALITY_CHANGED";
    
    // Priority constants
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_LOW = "LOW";
    
    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public Long getRecipientUserId() {
        return recipientUserId;
    }
    
    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Long getHostUserId() {
        return hostUserId;
    }
    
    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
    }
    
    public String getHostUsername() {
        return hostUsername;
    }
    
    public void setHostUsername(String hostUsername) {
        this.hostUsername = hostUsername;
    }
    
    public Long getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public String getSessionStatus() {
        return sessionStatus;
    }
    
    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }
    
    public Integer getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ScreenShareNotificationMessage{" +
                "notificationId='" + notificationId + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", recipientUserId=" + recipientUserId +
                ", sessionId='" + sessionId + '\'' +
                ", hostUsername='" + hostUsername + '\'' +
                ", title='" + title + '\'' +
                ", sessionStatus='" + sessionStatus + '\'' +
                ", priority='" + priority + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
