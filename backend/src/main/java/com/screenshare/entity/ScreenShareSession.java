package com.screenshare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "screen_share_sessions")
public class ScreenShareSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Session ID is required")
    @Column(nullable = false, unique = true, length = 100)
    private String sessionId;

    @NotNull(message = "Host user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(length = 200)
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.WAITING;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isPublic = true;

    @Column
    private Integer maxParticipants;

    @Column
    private Integer currentParticipants = 0;

    // Screen sharing quality settings
    @Column(length = 20)
    private String resolution; // e.g., "1920x1080", "1280x720"

    @Column
    private Integer frameRate; // frames per second

    @Column
    private Integer bitrate; // in kbps

    // WebRTC connection details
    @Column(length = 100)
    private String streamId;

    @Column(length = 50)
    private String audioEnabled = "false";

    // Participants viewing the screen share
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "session_participants",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    // Duration in seconds (calculated when session ends)
    @Column
    private Long durationSeconds;

    // Constructors
    public ScreenShareSession() {
    }

    public ScreenShareSession(String sessionId, User hostUser) {
        this.sessionId = sessionId;
        this.hostUser = hostUser;
    }

    // Helper methods
    public void addParticipant(User user) {
        this.participants.add(user);
        this.currentParticipants = this.participants.size();
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
        this.currentParticipants = this.participants.size();
    }

    public void startSession() {
        this.status = SessionStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
    }

    public void endSession() {
        this.status = SessionStatus.ENDED;
        this.endedAt = LocalDateTime.now();
        this.isActive = false;
        if (this.startedAt != null && this.endedAt != null) {
            this.durationSeconds = java.time.Duration.between(this.startedAt, this.endedAt).getSeconds();
        }
    }

    public void pauseSession() {
        this.status = SessionStatus.PAUSED;
    }

    public boolean isFull() {
        return maxParticipants != null && currentParticipants >= maxParticipants;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public User getHostUser() {
        return hostUser;
    }

    public void setHostUser(User hostUser) {
        this.hostUser = hostUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
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

    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getAudioEnabled() {
        return audioEnabled;
    }

    public void setAudioEnabled(String audioEnabled) {
        this.audioEnabled = audioEnabled;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
        this.currentParticipants = participants.size();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScreenShareSession that = (ScreenShareSession) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sessionId);
    }

    // toString
    @Override
    public String toString() {
        return "ScreenShareSession{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", hostUser=" + (hostUser != null ? hostUser.getUsername() : "null") +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", isActive=" + isActive +
                ", currentParticipants=" + currentParticipants +
                ", startedAt=" + startedAt +
                '}';
    }
}



