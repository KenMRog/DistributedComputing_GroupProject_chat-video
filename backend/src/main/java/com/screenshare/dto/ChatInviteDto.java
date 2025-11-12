package com.screenshare.dto;

import com.screenshare.entity.ChatInvite;
import com.screenshare.entity.InviteStatus;

import java.time.LocalDateTime;

public class ChatInviteDto {
    private Long id;
    private ChatRoomDto chatRoom;
    private UserDto invitedUser;
    private UserDto inviter;
    private InviteStatus status;
    private String message;
    private LocalDateTime expiresAt;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ChatInviteDto() {}

    public ChatInviteDto(ChatInvite invite) {
        this.id = invite.getId();
        this.chatRoom = new ChatRoomDto(invite.getChatRoom());
        this.invitedUser = new UserDto(invite.getInvitedUser());
        this.inviter = new UserDto(invite.getInviter());
        this.status = invite.getStatus();
        this.message = invite.getMessage();
        this.expiresAt = invite.getExpiresAt();
        this.respondedAt = invite.getRespondedAt();
        this.createdAt = invite.getCreatedAt();
        this.updatedAt = invite.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatRoomDto getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoomDto chatRoom) {
        this.chatRoom = chatRoom;
    }

    public UserDto getInvitedUser() {
        return invitedUser;
    }

    public void setInvitedUser(UserDto invitedUser) {
        this.invitedUser = invitedUser;
    }

    public UserDto getInviter() {
        return inviter;
    }

    public void setInviter(UserDto inviter) {
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
}
