package com.screenshare.service;

import com.screenshare.entity.*;
import com.screenshare.repository.ChatInviteRepository;
import com.screenshare.repository.ChatRoomRepository;
import com.screenshare.repository.ChatMessageRepository;
import com.screenshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatInviteRepository chatInviteRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    // Create a direct message chat between two users
    public ChatRoom createDirectMessageChat(Long userId1, Long userId2) {
        // Check if direct message already exists
        Optional<ChatRoom> existingRoom = chatRoomRepository.findDirectMessageRoom(userId1, userId2);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }
    // If no existing active DM, create an *inactive* chat room for the invite.
    User inviter = userRepository.findById(userId1)
        .orElseThrow(() -> new RuntimeException("User not found: " + userId1));
    User invited = userRepository.findById(userId2)
        .orElseThrow(() -> new RuntimeException("User not found: " + userId2));

    String roomCode = generateRoomCode();

    ChatRoom chatRoom = new ChatRoom();
    chatRoom.setRoomCode(roomCode);
    chatRoom.setRoomType(RoomType.DIRECT_MESSAGE);
    chatRoom.setMaxMembers(2);
    chatRoom.setCreatedBy(inviter);
    // Use invited user's display name as placeholder (room name will be set when activated)
    chatRoom.setName(invited.getDisplayName() != null ? invited.getDisplayName() : invited.getUsername());
    // IMPORTANT: keep the room inactive until the invite is accepted so it doesn't appear
    chatRoom.setIsActive(false);

    // Do NOT add members yet; members will be added when invite is accepted

    return chatRoomRepository.save(chatRoom);
    }

    // Get all chat rooms for a user
    public List<ChatRoom> getUserChatRooms(Long userId) {
        return chatRoomRepository.findRoomsByUserId(userId);
    }

    // Create a chat invite
    public ChatInvite createChatInvite(Long inviterId, Long invitedUserId, String message) {
        // Get users
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new RuntimeException("Inviter not found: " + inviterId));
        User invitedUser = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new RuntimeException("Invited user not found: " + invitedUserId));

        // Create or get direct message room
        ChatRoom chatRoom = createDirectMessageChat(inviterId, invitedUserId);

        // Check if invite already exists
        Optional<ChatInvite> existingInvite = chatInviteRepository.findPendingInvite(chatRoom.getId(), invitedUserId);
        if (existingInvite.isPresent()) {
            return existingInvite.get();
        }

        // Create invite
        ChatInvite invite = new ChatInvite(chatRoom, invitedUser, inviter, message);
        invite.setExpiresAt(LocalDateTime.now().plusDays(7)); // Expires in 7 days

        return chatInviteRepository.save(invite);
    }

    // Accept a chat invite
    public ChatRoom acceptInvite(Long inviteId, Long userId) {
        ChatInvite invite = chatInviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found: " + inviteId));

        if (!invite.getInvitedUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to accept this invite");
        }

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new RuntimeException("Invite is not pending");
        }

        if (invite.isExpired()) {
            invite.setStatus(InviteStatus.EXPIRED);
            chatInviteRepository.save(invite);
            throw new RuntimeException("Invite has expired");
        }

        // Accept the invite
        invite.accept();
        chatInviteRepository.save(invite);

        // Activate the chat room and add both users as members if not present
        ChatRoom chatRoom = invite.getChatRoom();
        // Activate the room so it becomes visible in chat lists
        if (!chatRoom.getIsActive()) {
            chatRoom.setIsActive(true);
        }

        // Ensure inviter is a member
        User inviter = invite.getInviter();
        if (!chatRoom.isMember(inviter)) {
            chatRoom.addMember(inviter);
            chatRoom.addAdmin(inviter);
        }

        // Add the invited user
        if (!chatRoom.isMember(invite.getInvitedUser())) {
            chatRoom.addMember(invite.getInvitedUser());
        }

        chatRoomRepository.save(chatRoom);

        return chatRoom;
    }

    // Decline a chat invite
    public void declineInvite(Long inviteId, Long userId) {
        ChatInvite invite = chatInviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found: " + inviteId));

        if (!invite.getInvitedUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to decline this invite");
        }

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new RuntimeException("Invite is not pending");
        }

        invite.decline();
        chatInviteRepository.save(invite);
    }

    // Get pending invites for a user
    public List<ChatInvite> getPendingInvites(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return chatInviteRepository.findByInvitedUserAndStatusOrderByCreatedAtDesc(user, InviteStatus.PENDING);
    }

    // Get all invites for a user
    public List<ChatInvite> getAllInvites(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return chatInviteRepository.findByInvitedUserOrderByCreatedAtDesc(user);
    }

    // Cancel an invite
    public void cancelInvite(Long inviteId, Long userId) {
        ChatInvite invite = chatInviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found: " + inviteId));

        if (!invite.getInviter().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to cancel this invite");
        }

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new RuntimeException("Invite is not pending");
        }

        invite.cancel();
        chatInviteRepository.save(invite);
    }

    // Get chat room by ID
    public Optional<ChatRoom> getChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId);
    }

    // Check if user is member of room
    public boolean isUserMemberOfRoom(Long roomId, Long userId) {
        return chatRoomRepository.isUserMemberOfRoom(roomId, userId);
    }

    // Generate unique room code
    private String generateRoomCode() {
        return "DM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Clean up expired invites (can be called by a scheduled task)
    public void cleanupExpiredInvites() {
        List<ChatInvite> expiredInvites = chatInviteRepository.findExpiredInvites(LocalDateTime.now());
        for (ChatInvite invite : expiredInvites) {
            invite.setStatus(InviteStatus.EXPIRED);
        }
        chatInviteRepository.saveAll(expiredInvites);
    }

    // Save a chat message to the database
    public ChatMessage saveMessage(Long roomId, Long senderId, String content, MessageType messageType) {
        // Get the chat room
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found: " + roomId));
        
        // Get the sender
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found: " + senderId));
        
        // Verify user is a member of the room and room is active
        if (!chatRoom.getIsActive() || !isUserMemberOfRoom(roomId, senderId)) {
            throw new RuntimeException("User is not authorized to send messages in this room");
        }
        
        // Create and save the message
        ChatMessage message = new ChatMessage(sender, content, chatRoom);
        message.setMessageType(messageType != null ? messageType : MessageType.TEXT);
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // Update room's last activity
        chatRoom.setLastActivityAt(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);
        
        return savedMessage;
    }

    // Get messages for a chat room
    public List<ChatMessage> getRoomMessages(Long roomId, Long userId) {
        // Verify user is a member of the room
        if (!isUserMemberOfRoom(roomId, userId)) {
            throw new RuntimeException("User is not authorized to view messages in this room");
        }
        
        return chatMessageRepository.findByChatRoomIdAndIsDeletedFalseOrderByCreatedAtAsc(roomId);
    }

    // Save a simple text message (convenience method)
    public ChatMessage saveTextMessage(Long roomId, Long senderId, String content) {
        return saveMessage(roomId, senderId, content, MessageType.TEXT);
    }
}
