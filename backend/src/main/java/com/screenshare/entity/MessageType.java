package com.screenshare.entity;

public enum MessageType {
    TEXT,           // Regular text message
    IMAGE,          // Image attachment
    FILE,           // File attachment
    VIDEO,          // Video attachment
    AUDIO,          // Audio/voice message
    SYSTEM,         // System-generated message (user joined, left, etc.)
    ANNOUNCEMENT,   // Important announcements
    CODE,           // Code snippet
    LINK            // Shared link
}

// only text is supported for now

