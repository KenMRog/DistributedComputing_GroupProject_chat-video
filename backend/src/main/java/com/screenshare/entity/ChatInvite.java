package com.screenshare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "chat_invites", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"chat_room_id", "invited_user_id"})
})
public class ChatInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Chat room is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @NotNull(message = "Invited user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id", nullable = false)
    private User invitedUser;

    @NotNull(message = "Inviter is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(length = 500)
    private String message; // Optional message from inviter

    @Column
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime respondedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public ChatInvite() {
    }

    public ChatInvite(ChatRoom chatRoom, User invitedUser, User inviter) {
        this.chatRoom = chatRoom;
        this.invitedUser = invitedUser;
        this.inviter = inviter;
    }

    public ChatInvite(ChatRoom chatRoom, User invitedUser, User inviter, String message) {
        this.chatRoom = chatRoom;
        this.invitedUser = invitedUser;
        this.inviter = inviter;
        this.message = message;
    }

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void accept() {
        this.status = InviteStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void decline() {
        this.status = InviteStatus.DECLINED;
        this.respondedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = InviteStatus.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public User getInvitedUser() {
        return invitedUser;
    }

    public void setInvitedUser(User invitedUser) {
        this.invitedUser = invitedUser;
    }

    public User getInviter() {
        return inviter;
    }

    public void setInviter(User inviter) {
        this.inviter = inviter;
    }

    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
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

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatInvite that = (ChatInvite) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "ChatInvite{" +
                "id=" + id +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
