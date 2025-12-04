package com.screenshare.controller;

import com.screenshare.dto.*;
import com.screenshare.entity.ChatRoom;
import com.screenshare.entity.ChatInvite;
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
import org.springframework.stereotype.Controller;

import java.util.Map;

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
                if (room.getIsActive() && (room.getRoomType() == com.screenshare.entity.RoomType.PUBLIC || chatService.isUserMemberOfRoom(roomId, message.getSenderId()))) {
                    
                    
                    MessageType msgType = MessageType.TEXT; 
                    if (message.getType() != null) {
                        try {
                            switch (message.getType()) {
                                case CHAT -> msgType = MessageType.TEXT;
                                case JOIN -> msgType = MessageType.SYSTEM;
                                case LEAVE -> msgType = MessageType.SYSTEM;
                                case SYSTEM -> msgType = MessageType.SYSTEM;
                                default -> msgType = MessageType.TEXT;
                            }
                        } catch (Exception e) {
                            System.err.println("Error converting message type: " + e.getMessage());
                            msgType = MessageType.TEXT; 
                        }
                    }
                    chatService.saveMessage(roomId, message.getSenderId(), message.getContent(), msgType);
                    
                    // Broadcast to all subscribers 
                    messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    @MessageMapping("/chat/{roomId}/addUser")
    public void addUserToRoom(@DestinationVariable Long roomId, ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        // Broadcast join event only if user is a member of the room and room is active
        if (chatService.getChatRoom(roomId).isPresent()) {
            ChatRoom room = chatService.getChatRoom(roomId).get();
            if (room.getIsActive() && (room.getRoomType() == com.screenshare.entity.RoomType.PUBLIC || chatService.isUserMemberOfRoom(roomId, message.getSenderId()))) {
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
            if (room.getIsActive() && (room.getRoomType() == com.screenshare.entity.RoomType.PUBLIC || chatService.isUserMemberOfRoom(roomId, message.getUserId()))) {
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

    // Relay WebRTC signaling messages between two peers in a chat room
    @MessageMapping("/signal/{roomId}")
    @SendTo("/topic/signal/{roomId}")
    public Map<String, Object> relaySignal(@DestinationVariable String roomId, Map<String, Object> payload) {
        // Simply forward the payload to all subscribers in that room
        return payload;
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

    // Create a new chat room 
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomDto> createRoom(@Valid @RequestBody CreateChatRoomRequest request, @RequestParam Long creatorId) {
        try {
            ChatRoom room = chatService.createGroupChat(creatorId, request.getName(), request.getDescription(), Boolean.TRUE.equals(request.getIsPrivate()));
            return ResponseEntity.ok(new ChatRoomDto(room));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Join a public room
    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<ChatRoomDto> joinRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        try {
            ChatRoom room = chatService.joinPublicRoom(roomId, userId);
            return ResponseEntity.ok(new ChatRoomDto(room));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Create a chat invite
    @PostMapping("/invite")
    public ResponseEntity<ChatInviteDto> createInvite(@Valid @RequestBody CreateChatInviteRequest request, 
                                                     @RequestParam Long inviterId) {
        try {
            ChatInvite invite = chatService.createChatInvite(
                    inviterId,
                    request.getInvitedUserId(),
                    request.getDescription()
            );
            ChatInviteDto inviteDto = new ChatInviteDto(invite);
            
            // Notify the invited user via WebSocket using user ID topic
            messagingTemplate.convertAndSend(
                "/topic/invites/" + invite.getInvitedUser().getId(),
                inviteDto
            );
            
            return ResponseEntity.ok(inviteDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Invite multiple users to an existing private room
    @PostMapping("/rooms/{roomId}/invites")
    public ResponseEntity<List<ChatInviteDto>> inviteUsersToRoom(@PathVariable Long roomId,
                                                                 @RequestBody com.screenshare.dto.InviteMultipleRequest request,
                                                                 @RequestParam Long inviterId) {
        try {
            List<ChatInviteDto> created = request.getInvitedUserIds().stream()
                    .map(uid -> {
                        ChatInvite invite = chatService.createChatInviteForRoom(inviterId, roomId, uid);
                        ChatInviteDto inviteDto = new ChatInviteDto(invite);
                        
                        // Notify the invited user via WebSocket using user ID topic
                        messagingTemplate.convertAndSend(
                            "/topic/invites/" + invite.getInvitedUser().getId(),
                            inviteDto
                        );
                        
                        return inviteDto;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(created);
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

    // Leave a room
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<String> leaveRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        try {
            chatService.leaveRoom(roomId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get chat room by ID
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomDto> getChatRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        try {
            // Fetch room
            java.util.Optional<ChatRoom> opt = chatService.getChatRoom(roomId);
            if (!opt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            ChatRoom room = opt.get();

            // Allow access if room is public or user is a member
            if (!room.getIsActive()) {
                return ResponseEntity.status(403).build();
            }

            if (room.getRoomType() == com.screenshare.entity.RoomType.PUBLIC || chatService.isUserMemberOfRoom(roomId, userId)) {
                return ResponseEntity.ok(new ChatRoomDto(room));
            }

            return ResponseEntity.status(403).build();
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
            CHAT, JOIN, LEAVE, SYSTEM
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
