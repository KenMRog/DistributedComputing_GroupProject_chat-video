package com.screenshare.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Notification message for chat-related notifications sent via Azure Service Bus
 * Used for real-time notifications to users about chat activity
 */
public class ChatNotificationMessage {
    
    @JsonProperty("notificationId")
    private String notificationId;
    
    @JsonProperty("notificationType")
    private String notificationType; // MESSAGE, INVITE, MENTION, ROOM_ACTIVITY
    
    @JsonProperty("recipientUserId")
    private Long recipientUserId;
    
    @JsonProperty("senderUserId")
    private Long senderUserId;
    
    @JsonProperty("senderUsername")
    private String senderUsername;
    
    @JsonProperty("roomId")
    private Long roomId;
    
    @JsonProperty("roomName")
    private String roomName;
    
    @JsonProperty("messageId")
    private Long messageId;
    
    @JsonProperty("messagePreview")
    private String messagePreview;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("body")
    private String body;
    
    @JsonProperty("priority")
    private String priority; // HIGH, NORMAL, LOW
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("expiresAt")
    private String expiresAt;
    
    // Constructors
    public ChatNotificationMessage() {
        this.timestamp = Instant.now().toString();
        this.priority = "NORMAL";
    }
    
    public ChatNotificationMessage(String notificationType, Long recipientUserId) {
        this();
        this.notificationType = notificationType;
        this.recipientUserId = recipientUserId;
        this.notificationId = java.util.UUID.randomUUID().toString();
    }
    
    // Notification type constants
    public static final String TYPE_NEW_MESSAGE = "NEW_MESSAGE";
    public static final String TYPE_INVITE_RECEIVED = "INVITE_RECEIVED";
    public static final String TYPE_INVITE_ACCEPTED = "INVITE_ACCEPTED";
    public static final String TYPE_MENTION = "MENTION";
    public static final String TYPE_ROOM_ACTIVITY = "ROOM_ACTIVITY";
    public static final String TYPE_USER_JOINED = "USER_JOINED";
    public static final String TYPE_USER_LEFT = "USER_LEFT";
    
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
    
    public Long getSenderUserId() {
        return senderUserId;
    }
    
    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }
    
    public String getSenderUsername() {
        return senderUsername;
    }
    
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
    
    public Long getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    
    public String getRoomName() {
        return roomName;
    }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    
    public Long getMessageId() {
        return messageId;
    }
    
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
    
    public String getMessagePreview() {
        return messagePreview;
    }
    
    public void setMessagePreview(String messagePreview) {
        this.messagePreview = messagePreview;
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
    
    public String getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    @Override
    public String toString() {
        return "ChatNotificationMessage{" +
                "notificationId='" + notificationId + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", recipientUserId=" + recipientUserId +
                ", senderUsername='" + senderUsername + '\'' +
                ", roomName='" + roomName + '\'' +
                ", title='" + title + '\'' +
                ", priority='" + priority + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
