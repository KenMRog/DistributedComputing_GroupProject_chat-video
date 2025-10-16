package com.screenshare.entity;

public enum SessionStatus {
    WAITING,      // Session created but not started
    ACTIVE,       // Session is currently active
    PAUSED,       // Session temporarily paused
    ENDED,        // Session has ended
    CANCELLED     // Session was cancelled
}



