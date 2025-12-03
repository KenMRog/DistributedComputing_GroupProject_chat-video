package com.screenshare.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.screenshare.config.WebSocketEventListener;
import com.screenshare.model.SignalMessage;

@Controller
public class ScreenShareController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/screenshare.start")
    @SendTo("/topic/screenshare")
    public ScreenShareMessage startScreenShare(ScreenShareMessage message) {
        message.setAction("start");
        return message;
    }

    @MessageMapping("/screenshare.stop")
    @SendTo("/topic/screenshare")
    public ScreenShareMessage stopScreenShare(ScreenShareMessage message) {
        message.setAction("stop");
        return message;
    }

    @MessageMapping("/screenshare.data")
    @SendTo("/topic/screenshare")
    public ScreenShareMessage handleScreenData(ScreenShareMessage message) {
        message.setAction("data");
        return message;
    }

    @MessageMapping("/screenshare.signal")
    public void handleSignal(SignalMessage message) {
        // Route signal to specific user if 'to' field is provided, otherwise broadcast
        if (message.getTo() != null && !message.getTo().isEmpty() && !message.getTo().equals("all")) {
            // Try to send using Spring's user destination feature
            // This requires the username to be registered in our session mapping
            // and the user to be subscribed to /user/{username}/queue/screenshare
            try {
                messagingTemplate.convertAndSendToUser(message.getTo(), "/queue/screenshare", message);
            } catch (Exception e) {
                // Fallback: if user destination doesn't work, try direct topic
                System.err.println("Failed to send to user " + message.getTo() + ": " + e.getMessage());
                messagingTemplate.convertAndSend("/topic/screenshare", message);
            }
        } else {
            // Broadcast to all subscribers (fallback)
            messagingTemplate.convertAndSend("/topic/screenshare", message);
        }
    }

    @MessageMapping("/screenshare.register")
    public void registerUser(@Header("simpSessionId") String sessionId,
                            @Payload String username) {
        // Register username with session for user-specific routing
        WebSocketEventListener.registerUser(username, sessionId);
    }

    public static class ScreenShareMessage {
        private String action;
        private String userId;
        private String roomId;
        private String data; 

        // Getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }
}
