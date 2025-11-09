package com.screenshare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateChatInviteRequest {
    
    @NotNull(message = "Invited user ID is required")
    private Long invitedUserId;
    
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;
    
    // Constructors
    public CreateChatInviteRequest() {}
    
    public CreateChatInviteRequest(Long invitedUserId, String message) {
        this.invitedUserId = invitedUserId;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getInvitedUserId() {
        return invitedUserId;
    }
    
    public void setInvitedUserId(Long invitedUserId) {
        this.invitedUserId = invitedUserId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
