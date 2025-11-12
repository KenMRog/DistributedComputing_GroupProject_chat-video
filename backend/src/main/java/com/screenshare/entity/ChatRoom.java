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
@Table(name = "chat_rooms", uniqueConstraints = {
    @UniqueConstraint(columnNames = "room_code")
})
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room code is required")
    @Column(nullable = false, unique = true, length = 50)
    private String roomCode;

    @NotBlank(message = "Room name is required")
    @Size(min = 1, max = 200, message = "Room name must be between 1 and 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 100, message = "Description must not exceed 100 characters")
    @Column(length = 100, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType roomType = RoomType.PUBLIC;

    @NotNull(message = "Room creator is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    // Members in the chat room
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "chat_room_members",
        joinColumns = @JoinColumn(name = "chat_room_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // Admins/moderators of the room
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "chat_room_admins",
        joinColumns = @JoinColumn(name = "chat_room_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> admins = new HashSet<>();

    // Optional: Link to a screen share session
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_share_session_id")
    private ScreenShareSession screenShareSession;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column
    private Integer maxMembers;

    @Column(nullable = false)
    private Integer currentMemberCount = 0;

    @Column(length = 500)
    private String iconUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastActivityAt;

    // Constructors
    public ChatRoom() {
    }

    public ChatRoom(String roomCode, String name, User createdBy) {
        this.roomCode = roomCode;
        this.name = name;
        this.createdBy = createdBy;
        this.admins.add(createdBy);
        this.members.add(createdBy);
        this.currentMemberCount = 1;
    }

    // Helper methods
    public void addMember(User user) {
        if (this.members.add(user)) {
            this.currentMemberCount = this.members.size();
            this.lastActivityAt = LocalDateTime.now();
        }
    }

    public void removeMember(User user) {
        if (this.members.remove(user)) {
            this.currentMemberCount = this.members.size();
            this.admins.remove(user); // Also remove from admins if they were one
            this.lastActivityAt = LocalDateTime.now();
        }
    }

    public void addAdmin(User user) {
        this.admins.add(user);
        this.members.add(user); // Admins must be members
        this.currentMemberCount = this.members.size();
    }

    public void removeAdmin(User user) {
        this.admins.remove(user);
    }

    public boolean isMember(User user) {
        return this.members.contains(user);
    }

    public boolean isAdmin(User user) {
        return this.admins.contains(user);
    }

    public boolean isFull() {
        return maxMembers != null && currentMemberCount >= maxMembers;
    }

    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.id != null && this.roomType == RoomType.PRIVATE && !Objects.equals(this.name, name)) {
            throw new IllegalStateException("Room name is immutable for private rooms");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (this.id != null && this.roomType == RoomType.PRIVATE && !Objects.equals(this.description, description)) {
            throw new IllegalStateException("Room description is immutable for private rooms");
        }
        this.description = description;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        if (this.id != null && this.roomType != roomType) {
            throw new IllegalStateException("Room type is immutable after creation");
        }
        this.roomType = roomType;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
        this.currentMemberCount = members.size();
    }

    public Set<User> getAdmins() {
        return admins;
    }

    public void setAdmins(Set<User> admins) {
        this.admins = admins;
    }

    public ScreenShareSession getScreenShareSession() {
        return screenShareSession;
    }

    public void setScreenShareSession(ScreenShareSession screenShareSession) {
        this.screenShareSession = screenShareSession;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Integer getCurrentMemberCount() {
        return currentMemberCount;
    }

    public void setCurrentMemberCount(Integer currentMemberCount) {
        this.currentMemberCount = currentMemberCount;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
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

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return Objects.equals(id, chatRoom.id) && 
               Objects.equals(roomCode, chatRoom.roomCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roomCode);
    }

    // toString
    @Override
    public String toString() {
        return "ChatRoom{" +
                "id=" + id +
                ", roomCode='" + roomCode + '\'' +
                ", name='" + name + '\'' +
                ", roomType=" + roomType +
                ", currentMemberCount=" + currentMemberCount +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}



