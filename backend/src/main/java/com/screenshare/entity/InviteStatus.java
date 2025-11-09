package com.screenshare.entity;

public enum InviteStatus {
    PENDING,    // Invite sent, waiting for response
    ACCEPTED,   // Invite accepted by user
    DECLINED,   // Invite declined by user
    CANCELLED,  // Invite cancelled by inviter
    EXPIRED     // Invite expired
}
