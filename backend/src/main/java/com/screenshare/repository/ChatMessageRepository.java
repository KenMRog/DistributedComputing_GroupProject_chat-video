package com.screenshare.repository;

import com.screenshare.entity.ChatMessage;
import com.screenshare.entity.ChatRoom;
import com.screenshare.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // Find messages in a chat room 
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.isDeleted = false ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByChatRoomIdAndIsDeletedFalseOrderByCreatedAtAsc(@Param("roomId") Long roomId);
    
    // Find messages in a chat room with pagination
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.isDeleted = false ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByChatRoomIdAndIsDeletedFalse(@Param("roomId") Long roomId, Pageable pageable);
    
    // Find messages by sender
    List<ChatMessage> findBySenderAndIsDeletedFalseOrderByCreatedAtDesc(User sender);
    
    // Find messages after a certain time
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.createdAt > :timestamp AND cm.isDeleted = false ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByChatRoomIdAndCreatedAtAfterAndIsDeletedFalse(@Param("roomId") Long roomId, @Param("timestamp") LocalDateTime timestamp);
    
    // Count unread messages for a user in a room 
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.sender.id != :userId AND cm.isDeleted = false")
    long countUnreadMessagesInRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);
    
    // Find latest message in a chat room
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.isDeleted = false ORDER BY cm.createdAt DESC")
    List<ChatMessage> findLatestMessageInRoom(@Param("roomId") Long roomId, Pageable pageable);
}