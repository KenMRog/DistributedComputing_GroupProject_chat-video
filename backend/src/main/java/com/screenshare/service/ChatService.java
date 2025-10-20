package com.screenshare.service;

import com.screenshare.entity.*;
import com.screenshare.repository.ChatInviteRepository;
import com.screenshare.repository.ChatRoomRepository;
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
    private UserRepository userRepository;

    // Create a direct message chat between two users
    public ChatRoom createDirectMessageChat(Long userId1, Long userId2) {
        // Check if direct message already exists
        Optional<ChatRoom> existingRoom = chatRoomRepository.findDirectMessageRoom(userId1, userId2);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // Get users
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId1));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId2));

        // Create room code
        String roomCode = generateRoomCode();

        // Create chat room
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomCode(roomCode);
        chatRoom.setRoomType(RoomType.DIRECT_MESSAGE);
        chatRoom.setMaxMembers(2);
        chatRoom.setCreatedBy(user1);
        chatRoom.setName(user2.getDisplayName() != null ? user2.getDisplayName() : user2.getUsername());
        chatRoom.setIsActive(true);

        // Add both users as members
        chatRoom.addMember(user1);
        chatRoom.addMember(user2);
        chatRoom.addAdmin(user1); // Creator is admin

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

        // Add user to chat room if not already a member
        ChatRoom chatRoom = invite.getChatRoom();
        if (!chatRoom.isMember(invite.getInvitedUser())) {
            chatRoom.addMember(invite.getInvitedUser());
            chatRoomRepository.save(chatRoom);
        }

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
}
