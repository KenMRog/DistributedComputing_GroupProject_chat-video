package com.screenshare.dto;

import com.screenshare.entity.ChatRoom;
import com.screenshare.entity.RoomType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ChatRoomDto {
    private Long id;
    private String roomCode;
    private String name;
    private String description;
    private RoomType roomType;
    private Long createdById;
    private String createdByUsername;
    private List<UserDto> members;
    private List<UserDto> admins;
    private Boolean isActive;
    private Integer maxMembers;
    private Integer currentMemberCount;
    private String iconUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActivityAt;

    // Constructors
    public ChatRoomDto() {}

    public ChatRoomDto(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.roomCode = chatRoom.getRoomCode();
        this.name = chatRoom.getName();
        this.description = chatRoom.getDescription();
        this.roomType = chatRoom.getRoomType();
        this.createdById = chatRoom.getCreatedBy().getId();
        this.createdByUsername = chatRoom.getCreatedBy().getUsername();
        this.members = chatRoom.getMembers().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
        this.admins = chatRoom.getAdmins().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
        this.isActive = chatRoom.getIsActive();
        this.maxMembers = chatRoom.getMaxMembers();
        this.currentMemberCount = chatRoom.getCurrentMemberCount();
        this.iconUrl = chatRoom.getIconUrl();
        this.createdAt = chatRoom.getCreatedAt();
        this.updatedAt = chatRoom.getUpdatedAt();
        this.lastActivityAt = chatRoom.getLastActivityAt();
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
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public List<UserDto> getMembers() {
        return members;
    }

    public void setMembers(List<UserDto> members) {
        this.members = members;
    }

    public List<UserDto> getAdmins() {
        return admins;
    }

    public void setAdmins(List<UserDto> admins) {
        this.admins = admins;
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
}
