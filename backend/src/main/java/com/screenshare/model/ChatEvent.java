package com.screenshare.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Event model for chat-related events published to Azure Event Grid
 * Represents domain events like message sent, user joined, room created, etc.
 */
public class ChatEvent {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("subject")
    private String subject; // e.g., "chat/room/123" or "chat/user/456"
    
    @JsonProperty("eventType")
    private String eventType; // e.g., "chat.message.sent", "chat.user.joined"
    
    @JsonProperty("eventTime")
    private String eventTime;
    
    @JsonProperty("dataVersion")
    private String dataVersion = "1.0";
    
    @JsonProperty("data")
    private ChatEventData data;
    
    // Constructors
    public ChatEvent() {
        this.id = UUID.randomUUID().toString();
        this.eventTime = Instant.now().toString();
    }
    
    public ChatEvent(String subject, String eventType, ChatEventData data) {
        this();
        this.subject = subject;
        this.eventType = eventType;
        this.data = data;
    }
    
    // Event type constants
    public static final String MESSAGE_SENT = "chat.message.sent";
    public static final String MESSAGE_EDITED = "chat.message.edited";
    public static final String MESSAGE_DELETED = "chat.message.deleted";
    public static final String USER_JOINED_ROOM = "chat.user.joined";
    public static final String USER_LEFT_ROOM = "chat.user.left";
    public static final String ROOM_CREATED = "chat.room.created";
    public static final String INVITE_SENT = "chat.invite.sent";
    public static final String INVITE_ACCEPTED = "chat.invite.accepted";
    public static final String INVITE_DECLINED = "chat.invite.declined";
    
    // Nested data class
    public static class ChatEventData {
        @JsonProperty("roomId")
        private Long roomId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("username")
        private String username;
        
        @JsonProperty("messageId")
        private Long messageId;
        
        @JsonProperty("messageContent")
        private String messageContent;
        
        @JsonProperty("messageType")
        private String messageType;
        
        @JsonProperty("roomName")
        private String roomName;
        
        @JsonProperty("roomType")
        private String roomType;
        
        @JsonProperty("inviteId")
        private Long inviteId;
        
        @JsonProperty("invitedUserId")
        private Long invitedUserId;
        
        @JsonProperty("timestamp")
        private String timestamp;
        
        // Constructors
        public ChatEventData() {
            this.timestamp = Instant.now().toString();
        }
        
        // Getters and Setters
        public Long getRoomId() {
            return roomId;
        }
        
        public void setRoomId(Long roomId) {
            this.roomId = roomId;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public Long getMessageId() {
            return messageId;
        }
        
        public void setMessageId(Long messageId) {
            this.messageId = messageId;
        }
        
        public String getMessageContent() {
            return messageContent;
        }
        
        public void setMessageContent(String messageContent) {
            this.messageContent = messageContent;
        }
        
        public String getMessageType() {
            return messageType;
        }
        
        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }
        
        public String getRoomName() {
            return roomName;
        }
        
        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }
        
        public String getRoomType() {
            return roomType;
        }
        
        public void setRoomType(String roomType) {
            this.roomType = roomType;
        }
        
        public Long getInviteId() {
            return inviteId;
        }
        
        public void setInviteId(Long inviteId) {
            this.inviteId = inviteId;
        }
        
        public Long getInvitedUserId() {
            return invitedUserId;
        }
        
        public void setInvitedUserId(Long invitedUserId) {
            this.invitedUserId = invitedUserId;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return "ChatEventData{" +
                    "roomId=" + roomId +
                    ", userId=" + userId +
                    ", username='" + username + '\'' +
                    ", messageId=" + messageId +
                    ", roomName='" + roomName + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        }
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getEventTime() {
        return eventTime;
    }
    
    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }
    
    public String getDataVersion() {
        return dataVersion;
    }
    
    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }
    
    public ChatEventData getData() {
        return data;
    }
    
    public void setData(ChatEventData data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "ChatEvent{" +
                "id='" + id + '\'' +
                ", subject='" + subject + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventTime='" + eventTime + '\'' +
                ", data=" + data +
                '}';
    }
}
