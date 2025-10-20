package com.screenshare.repository;

import com.screenshare.entity.ChatRoom;
import com.screenshare.entity.RoomType;
import com.screenshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    // Find rooms where user is a member
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members m WHERE m.id = :userId AND cr.isActive = true ORDER BY cr.lastActivityAt DESC")
    List<ChatRoom> findRoomsByUserId(@Param("userId") Long userId);
    
    // Find direct message room between two users
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.roomType = 'DIRECT_MESSAGE' AND cr.isActive = true " +
           "AND EXISTS (SELECT 1 FROM cr.members m1 WHERE m1.id = :userId1) " +
           "AND EXISTS (SELECT 1 FROM cr.members m2 WHERE m2.id = :userId2) " +
           "AND cr.currentMemberCount = 2")
    Optional<ChatRoom> findDirectMessageRoom(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    // Find room by room code
    Optional<ChatRoom> findByRoomCode(String roomCode);
    
    // Find rooms by type
    List<ChatRoom> findByRoomTypeAndIsActiveTrue(RoomType roomType);
    
    // Find rooms created by user
    List<ChatRoom> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(User createdBy);
    
    // Check if user is member of room
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM ChatRoom cr JOIN cr.members m WHERE cr.id = :roomId AND m.id = :userId")
    boolean isUserMemberOfRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
