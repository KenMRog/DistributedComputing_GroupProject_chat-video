package com.screenshare.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Event model for screen share events published to Azure Event Grid
 * Represents domain events like session started, participant joined, session ended, etc.
 */
public class ScreenShareEvent {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("subject")
    private String subject; // e.g., "screenshare/session/123"
    
    @JsonProperty("eventType")
    private String eventType; // e.g., "screenshare.session.started"
    
    @JsonProperty("eventTime")
    private String eventTime;
    
    @JsonProperty("dataVersion")
    private String dataVersion = "1.0";
    
    @JsonProperty("data")
    private ScreenShareEventData data;
    
    // Constructors
    public ScreenShareEvent() {
        this.id = UUID.randomUUID().toString();
        this.eventTime = Instant.now().toString();
    }
    
    public ScreenShareEvent(String subject, String eventType, ScreenShareEventData data) {
        this();
        this.subject = subject;
        this.eventType = eventType;
        this.data = data;
    }
    
    // Event type constants
    public static final String SESSION_STARTED = "screenshare.session.started";
    public static final String SESSION_ENDED = "screenshare.session.ended";
    public static final String SESSION_PAUSED = "screenshare.session.paused";
    public static final String SESSION_RESUMED = "screenshare.session.resumed";
    public static final String PARTICIPANT_JOINED = "screenshare.participant.joined";
    public static final String PARTICIPANT_LEFT = "screenshare.participant.left";
    public static final String QUALITY_CHANGED = "screenshare.quality.changed";
    
    // Nested data class
    public static class ScreenShareEventData {
        @JsonProperty("sessionId")
        private String sessionId;
        
        @JsonProperty("hostUserId")
        private Long hostUserId;
        
        @JsonProperty("hostUsername")
        private String hostUsername;
        
        @JsonProperty("roomId")
        private Long roomId;
        
        @JsonProperty("participantUserId")
        private Long participantUserId;
        
        @JsonProperty("participantUsername")
        private String participantUsername;
        
        @JsonProperty("participantCount")
        private Integer participantCount;
        
        @JsonProperty("sessionStatus")
        private String sessionStatus;
        
        @JsonProperty("resolution")
        private String resolution;
        
        @JsonProperty("frameRate")
        private Integer frameRate;
        
        @JsonProperty("timestamp")
        private String timestamp;
        
        // Constructors
        public ScreenShareEventData() {
            this.timestamp = Instant.now().toString();
        }
        
        // Getters and Setters
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
        
        public Long getParticipantUserId() {
            return participantUserId;
        }
        
        public void setParticipantUserId(Long participantUserId) {
            this.participantUserId = participantUserId;
        }
        
        public String getParticipantUsername() {
            return participantUsername;
        }
        
        public void setParticipantUsername(String participantUsername) {
            this.participantUsername = participantUsername;
        }
        
        public Integer getParticipantCount() {
            return participantCount;
        }
        
        public void setParticipantCount(Integer participantCount) {
            this.participantCount = participantCount;
        }
        
        public String getSessionStatus() {
            return sessionStatus;
        }
        
        public void setSessionStatus(String sessionStatus) {
            this.sessionStatus = sessionStatus;
        }
        
        public String getResolution() {
            return resolution;
        }
        
        public void setResolution(String resolution) {
            this.resolution = resolution;
        }
        
        public Integer getFrameRate() {
            return frameRate;
        }
        
        public void setFrameRate(Integer frameRate) {
            this.frameRate = frameRate;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return "ScreenShareEventData{" +
                    "sessionId='" + sessionId + '\'' +
                    ", hostUserId=" + hostUserId +
                    ", hostUsername='" + hostUsername + '\'' +
                    ", roomId=" + roomId +
                    ", participantCount=" + participantCount +
                    ", sessionStatus='" + sessionStatus + '\'' +
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
    
    public ScreenShareEventData getData() {
        return data;
    }
    
    public void setData(ScreenShareEventData data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "ScreenShareEvent{" +
                "id='" + id + '\'' +
                ", subject='" + subject + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventTime='" + eventTime + '\'' +
                ", data=" + data +
                '}';
    }
}
