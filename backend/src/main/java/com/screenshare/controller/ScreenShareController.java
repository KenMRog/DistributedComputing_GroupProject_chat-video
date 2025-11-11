package com.screenshare.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.screenshare.model.SignalMessage;

@Controller
public class ScreenShareController {

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
    @SendTo("/topic/screenshare")
    public SignalMessage handleSignal(SignalMessage message) {
        // Simply broadcast the message to all subscribers
        return message;
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
