package com.screenshare.repository;

import com.screenshare.entity.ChatInvite;
import com.screenshare.entity.InviteStatus;
import com.screenshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatInviteRepository extends JpaRepository<ChatInvite, Long> {
    
    // Find pending invites for a user
    List<ChatInvite> findByInvitedUserAndStatusOrderByCreatedAtDesc(User invitedUser, InviteStatus status);
    
    // Find all invites for a user (any status)
    List<ChatInvite> findByInvitedUserOrderByCreatedAtDesc(User invitedUser);
    
    // Find invites sent by a user
    List<ChatInvite> findByInviterOrderByCreatedAtDesc(User inviter);
    
    // Find specific invite
    @Query("SELECT ci FROM ChatInvite ci WHERE ci.chatRoom.id = :roomId AND ci.invitedUser.id = :userId AND ci.status = 'PENDING'")
    Optional<ChatInvite> findPendingInvite(@Param("roomId") Long roomId, @Param("userId") Long userId);
    
    // Find expired invites
    @Query("SELECT ci FROM ChatInvite ci WHERE ci.status = 'PENDING' AND ci.expiresAt < :now")
    List<ChatInvite> findExpiredInvites(@Param("now") LocalDateTime now);
    
    // Count pending invites for a user
    long countByInvitedUserAndStatus(User invitedUser, InviteStatus status);
    
    // Check if invite exists
    boolean existsByChatRoomIdAndInvitedUserIdAndStatus(Long chatRoomId, Long invitedUserId, InviteStatus status);
}
