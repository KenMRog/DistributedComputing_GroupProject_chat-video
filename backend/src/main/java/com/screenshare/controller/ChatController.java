package com.screenshare.controller;

import com.screenshare.dto.*;
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
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    // WebSocket message handlers
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(ChatMessage message) {
        message.setContent(message.getSender() + " joined!");
        message.setTimestamp(LocalDateTime.now());
        return message;
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

    // WebSocket message class
    public static class ChatMessage {
        private String content;
        private String sender;
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
        
        public MessageType getType() { return type; }
        public void setType(MessageType type) { this.type = type; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
