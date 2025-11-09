package com.screenshare.controller;

import com.screenshare.dto.*;
import com.screenshare.entity.ChatRoom;
import com.screenshare.entity.MessageType;
import com.screenshare.entity.User;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.screenshare.service.ChatService;
import com.screenshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // WebSocket message handlers
    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessageToRoom(@DestinationVariable Long roomId, ChatMessage message) {
        try {
            // Set timestamp
            message.setTimestamp(LocalDateTime.now());

            // Only proceed if room exists and is active and sender is a member
            if (chatService.getChatRoom(roomId).isPresent()) {
                ChatRoom room = chatService.getChatRoom(roomId).get();
                if (room.getIsActive() && chatService.isUserMemberOfRoom(roomId, message.getSenderId())) {
                    
                    // Save the message to database (this persists it even if users are offline)
                    // Convert WebSocket MessageType to Entity MessageType
                    MessageType msgType = MessageType.TEXT; // Default to TEXT
                    if (message.getType() != null) {
                        try {
                            switch (message.getType()) {
                                case CHAT -> msgType = MessageType.TEXT;
                                case JOIN -> msgType = MessageType.SYSTEM;
                                case LEAVE -> msgType = MessageType.SYSTEM;
                                default -> msgType = MessageType.TEXT;
                            }
                        } catch (Exception e) {
                            System.err.println("Error converting message type: " + e.getMessage());
                            msgType = MessageType.TEXT; // fallback to TEXT
                        }
                    }
                    chatService.saveMessage(roomId, message.getSenderId(), message.getContent(), msgType);
                    
                    // Broadcast to all subscribers (online users will see it immediately)
                    messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
                }
            }
        } catch (Exception e) {
            // Log error but don't crash the WebSocket connection
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    @MessageMapping("/chat/{roomId}/addUser")
    public void addUserToRoom(@DestinationVariable Long roomId, ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        // Broadcast join event only if user is a member of the room and room is active
        if (chatService.getChatRoom(roomId).isPresent()) {
            ChatRoom room = chatService.getChatRoom(roomId).get();
            if (room.getIsActive() && chatService.isUserMemberOfRoom(roomId, message.getSenderId())) {
                // Set content for join message if not already set
                if (message.getContent() == null || message.getContent().isEmpty()) {
                    message.setContent(message.getSender() + " joined!");
                }
                messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
            }
        }
    }

    @MessageMapping("/screenshare/{roomId}/start")
    public void startScreenShare(@DestinationVariable Long roomId, ScreenShareMessage message) {
        // Verify user is member of room and room is active
        if (chatService.getChatRoom(roomId).isPresent()) {
            ChatRoom room = chatService.getChatRoom(roomId).get();
            if (room.getIsActive() && chatService.isUserMemberOfRoom(roomId, message.getUserId())) {
                message.setAction("start");
                message.setTimestamp(LocalDateTime.now());
                messagingTemplate.convertAndSend("/topic/screenshare/" + roomId, message);
            }
        }
    }

    @MessageMapping("/screenshare/{roomId}/stop")
    public void stopScreenShare(@DestinationVariable Long roomId, ScreenShareMessage message) {
        // Verify user is member of room and room is active
        if (chatService.getChatRoom(roomId).isPresent()) {
            ChatRoom room = chatService.getChatRoom(roomId).get();
            if (room.getIsActive() && chatService.isUserMemberOfRoom(roomId, message.getUserId())) {
                message.setAction("stop");
                message.setTimestamp(LocalDateTime.now());
                messagingTemplate.convertAndSend("/topic/screenshare/" + roomId, message);
            }
        }
    }

    // REST API endpoints

    // Get all chat rooms for current user
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDto>> getUserChatRooms(@RequestParam Long userId) {
        try {
            List<ChatRoomDto> rooms = chatService.getUserChatRooms(userId)
                    .stream()
                    .map(ChatRoomDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Create a chat invite
    @PostMapping("/invite")
    public ResponseEntity<ChatInviteDto> createInvite(@Valid @RequestBody CreateChatInviteRequest request, 
                                                     @RequestParam Long inviterId) {
        try {
            ChatInviteDto invite = new ChatInviteDto(chatService.createChatInvite(
                    inviterId, 
                    request.getInvitedUserId(), 
                    request.getMessage()
            ));
            return ResponseEntity.ok(invite);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get pending invites for user
    @GetMapping("/invites/pending")
    public ResponseEntity<List<ChatInviteDto>> getPendingInvites(@RequestParam Long userId) {
        try {
            List<ChatInviteDto> invites = chatService.getPendingInvites(userId)
                    .stream()
                    .map(ChatInviteDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(invites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all invites for user
    @GetMapping("/invites")
    public ResponseEntity<List<ChatInviteDto>> getAllInvites(@RequestParam Long userId) {
        try {
            List<ChatInviteDto> invites = chatService.getAllInvites(userId)
                    .stream()
                    .map(ChatInviteDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(invites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Accept an invite
    @PostMapping("/invite/{inviteId}/accept")
    public ResponseEntity<ChatRoomDto> acceptInvite(@PathVariable Long inviteId, @RequestParam Long userId) {
        try {
            ChatRoomDto room = new ChatRoomDto(chatService.acceptInvite(inviteId, userId));
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Decline an invite
    @PostMapping("/invite/{inviteId}/decline")
    public ResponseEntity<Void> declineInvite(@PathVariable Long inviteId, @RequestParam Long userId) {
        try {
            chatService.declineInvite(inviteId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Cancel an invite
    @PostMapping("/invite/{inviteId}/cancel")
    public ResponseEntity<Void> cancelInvite(@PathVariable Long inviteId, @RequestParam Long userId) {
        try {
            chatService.cancelInvite(inviteId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get chat room by ID
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomDto> getChatRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        try {
            if (!chatService.isUserMemberOfRoom(roomId, userId)) {
                return ResponseEntity.status(403).build();
            }
            
            return chatService.getChatRoom(roomId)
                    .map(room -> ResponseEntity.ok(new ChatRoomDto(room)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get messages for a chat room
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getRoomMessages(@PathVariable Long roomId, @RequestParam Long userId) {
        try {
            List<ChatMessageDto> messages = chatService.getRoomMessages(roomId, userId)
                    .stream()
                    .map(ChatMessageDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }

    // WebSocket message class
    public static class ChatMessage {
        private String content;
        private String sender;
        private Long senderId;
        private MessageType type;
        private LocalDateTime timestamp;

        public enum MessageType {
            CHAT, JOIN, LEAVE
        }

        // Getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }
        
        public MessageType getType() { return type; }
        public void setType(MessageType type) { this.type = type; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // Screen share message class
    public static class ScreenShareMessage {
        private Long userId;
        private String username;
        private Long roomId;
        private String action;
        private LocalDateTime timestamp;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
