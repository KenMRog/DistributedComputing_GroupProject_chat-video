package com.screenshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateChatInviteRequest {
    
    @NotNull(message = "Invited user ID is required")
    private Long invitedUserId;
    
    @NotBlank(message = "Description is required")
    @Size(max = 100, message = "Description must not exceed 100 characters")
    private String description;
    
    // Constructors
    public CreateChatInviteRequest() {}
    
    public CreateChatInviteRequest(Long invitedUserId, String description) {
        this.invitedUserId = invitedUserId;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getInvitedUserId() {
        return invitedUserId;
    }
    
    public void setInvitedUserId(Long invitedUserId) {
        this.invitedUserId = invitedUserId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
