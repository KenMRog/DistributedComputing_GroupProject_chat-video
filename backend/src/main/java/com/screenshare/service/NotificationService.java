package com.screenshare.service;

import com.screenshare.entity.*;
import com.screenshare.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Unified notification service that orchestrates Azure Event Grid and Service Bus
 * for the chatroom and screen sharing application.
 * 
 * Event Grid: Used for publishing domain events (audit trail, analytics, integration)
 * Service Bus Queue: Used for guaranteed delivery of direct notifications to specific users
 * Service Bus Topic: Used for broadcasting notifications to multiple subscribers
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired(required = false)
    private AzureEventGridPublisher eventGridPublisher;
    
    @Autowired(required = false)
    private AzureServiceBusProducer serviceBusProducer;
    
    // ===== CHAT EVENT NOTIFICATIONS =====
    
    /**
     * Notify when a new message is sent in a chat room
     */
    public void notifyMessageSent(ChatMessage message, ChatRoom chatRoom) {
        try {
            // Publish domain event to Event Grid
            if (eventGridPublisher != null) {
                ChatEvent event = new ChatEvent(
                    "chat/room/" + chatRoom.getId(),
                    ChatEvent.MESSAGE_SENT,
                    createChatEventData(message, chatRoom)
                );
                eventGridPublisher.publishChatEvent(event);
            }
            
            // Send notifications to room members via Service Bus Queue
            if (serviceBusProducer != null && chatRoom.getMembers() != null) {
                for (User member : chatRoom.getMembers()) {
                    // Don't notify the sender
                    if (!member.getId().equals(message.getSender().getId())) {
                        ChatNotificationMessage notification = new ChatNotificationMessage(
                            ChatNotificationMessage.TYPE_NEW_MESSAGE,
                            member.getId()
                        );
                        notification.setSenderUserId(message.getSender().getId());
                        notification.setSenderUsername(message.getSender().getUsername());
                        notification.setRoomId(chatRoom.getId());
                        notification.setRoomName(chatRoom.getName());
                        notification.setMessageId(message.getId());
                        notification.setMessagePreview(truncate(message.getContent(), 100));
                        notification.setTitle("New message in " + chatRoom.getName());
                        notification.setBody(message.getSender().getUsername() + ": " + truncate(message.getContent(), 50));
                        
                        serviceBusProducer.sendChatNotification(notification);
                    }
                }
            }
            
            logger.debug("Notified message sent for room {} by user {}", 
                chatRoom.getId(), message.getSender().getUsername());
        } catch (Exception e) {
            logger.error("Error sending message notifications", e);
        }
    }
    
    /**
     * Notify when a user joins a chat room
     */
    public void notifyUserJoinedRoom(User user, ChatRoom chatRoom) {
        try {
            // Publish domain event
            if (eventGridPublisher != null) {
                ChatEvent.ChatEventData eventData = new ChatEvent.ChatEventData();
                eventData.setRoomId(chatRoom.getId());
                eventData.setUserId(user.getId());
                eventData.setUsername(user.getUsername());
                eventData.setRoomName(chatRoom.getName());
                eventData.setRoomType(chatRoom.getRoomType().toString());
                
                ChatEvent event = new ChatEvent(
                    "chat/room/" + chatRoom.getId(),
                    ChatEvent.USER_JOINED_ROOM,
                    eventData
                );
                eventGridPublisher.publishChatEvent(event);
            }
            
            // Broadcast to room members
            if (serviceBusProducer != null && chatRoom.getMembers() != null) {
                for (User member : chatRoom.getMembers()) {
                    if (!member.getId().equals(user.getId())) {
                        ChatNotificationMessage notification = new ChatNotificationMessage(
                            ChatNotificationMessage.TYPE_USER_JOINED,
                            member.getId()
                        );
                        notification.setRoomId(chatRoom.getId());
                        notification.setRoomName(chatRoom.getName());
                        notification.setSenderUserId(user.getId());
                        notification.setSenderUsername(user.getUsername());
                        notification.setTitle(chatRoom.getName());
                        notification.setBody(user.getUsername() + " joined the room");
                        notification.setPriority(ChatNotificationMessage.PRIORITY_LOW);
                        
                        serviceBusProducer.sendChatNotification(notification);
                    }
                }
            }
            
            logger.debug("Notified user {} joined room {}", user.getUsername(), chatRoom.getId());
        } catch (Exception e) {
            logger.error("Error sending user joined notifications", e);
        }
    }
    
    /**
     * Notify when a chat invite is sent
     */
    public void notifyInviteSent(ChatInvite invite) {
        try {
            // Publish domain event
            if (eventGridPublisher != null) {
                ChatEvent.ChatEventData eventData = new ChatEvent.ChatEventData();
                eventData.setInviteId(invite.getId());
                eventData.setUserId(invite.getInviter().getId());
                eventData.setUsername(invite.getInviter().getUsername());
                eventData.setInvitedUserId(invite.getInvitedUser().getId());
                eventData.setRoomId(invite.getChatRoom().getId());
                eventData.setRoomName(invite.getChatRoom().getName());
                
                ChatEvent event = new ChatEvent(
                    "chat/invite/" + invite.getId(),
                    ChatEvent.INVITE_SENT,
                    eventData
                );
                eventGridPublisher.publishChatEvent(event);
            }
            
            // Send notification to invited user
            if (serviceBusProducer != null) {
                ChatNotificationMessage notification = new ChatNotificationMessage(
                    ChatNotificationMessage.TYPE_INVITE_RECEIVED,
                    invite.getInvitedUser().getId()
                );
                notification.setSenderUserId(invite.getInviter().getId());
                notification.setSenderUsername(invite.getInviter().getUsername());
                notification.setRoomId(invite.getChatRoom().getId());
                notification.setRoomName(invite.getChatRoom().getName());
                notification.setTitle("New chat invite");
                notification.setBody(invite.getInviter().getUsername() + " invited you to " + invite.getChatRoom().getName());
                notification.setPriority(ChatNotificationMessage.PRIORITY_HIGH);
                
                serviceBusProducer.sendChatNotification(notification);
            }
            
            logger.debug("Notified invite sent from {} to {}", 
                invite.getInviter().getUsername(), invite.getInvitedUser().getUsername());
        } catch (Exception e) {
            logger.error("Error sending invite notifications", e);
        }
    }
    
    /**
     * Notify when a chat invite is accepted
     */
    public void notifyInviteAccepted(ChatInvite invite) {
        try {
            // Publish domain event
            if (eventGridPublisher != null) {
                ChatEvent.ChatEventData eventData = new ChatEvent.ChatEventData();
                eventData.setInviteId(invite.getId());
                eventData.setUserId(invite.getInvitedUser().getId());
                eventData.setUsername(invite.getInvitedUser().getUsername());
                eventData.setRoomId(invite.getChatRoom().getId());
                eventData.setRoomName(invite.getChatRoom().getName());
                
                ChatEvent event = new ChatEvent(
                    "chat/invite/" + invite.getId(),
                    ChatEvent.INVITE_ACCEPTED,
                    eventData
                );
                eventGridPublisher.publishChatEvent(event);
            }
            
            // Notify the inviter
            if (serviceBusProducer != null) {
                ChatNotificationMessage notification = new ChatNotificationMessage(
                    ChatNotificationMessage.TYPE_INVITE_ACCEPTED,
                    invite.getInviter().getId()
                );
                notification.setSenderUserId(invite.getInvitedUser().getId());
                notification.setSenderUsername(invite.getInvitedUser().getUsername());
                notification.setRoomId(invite.getChatRoom().getId());
                notification.setRoomName(invite.getChatRoom().getName());
                notification.setTitle("Invite accepted");
                notification.setBody(invite.getInvitedUser().getUsername() + " accepted your invite to " + invite.getChatRoom().getName());
                
                serviceBusProducer.sendChatNotification(notification);
            }
            
            logger.debug("Notified invite accepted for invite {}", invite.getId());
        } catch (Exception e) {
            logger.error("Error sending invite accepted notifications", e);
        }
    }
    
    // ===== SCREEN SHARE EVENT NOTIFICATIONS =====
    
    /**
     * Notify when a screen share session starts
     */
    public void notifySessionStarted(ScreenShareSession session, ChatRoom chatRoom) {
        try {
            // Publish domain event
            if (eventGridPublisher != null) {
                ScreenShareEvent.ScreenShareEventData eventData = new ScreenShareEvent.ScreenShareEventData();
                eventData.setSessionId(session.getSessionId());
                eventData.setHostUserId(session.getHostUser().getId());
                eventData.setHostUsername(session.getHostUser().getUsername());
                if (chatRoom != null) {
                    eventData.setRoomId(chatRoom.getId());
                }
                eventData.setSessionStatus(session.getStatus().toString());
                eventData.setParticipantCount(session.getCurrentParticipants());
                eventData.setResolution(session.getResolution());
                eventData.setFrameRate(session.getFrameRate());
                
                ScreenShareEvent event = new ScreenShareEvent(
                    "screenshare/session/" + session.getSessionId(),
                    ScreenShareEvent.SESSION_STARTED,
                    eventData
                );
                eventGridPublisher.publishScreenShareEvent(event);
            }
            
            // Broadcast to room members or all participants
            if (serviceBusProducer != null) {
                Set<User> recipients = chatRoom != null ? chatRoom.getMembers() : session.getParticipants();
                if (recipients != null) {
                    for (User member : recipients) {
                        if (!member.getId().equals(session.getHostUser().getId())) {
                            ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                                ScreenShareNotificationMessage.TYPE_SESSION_STARTED,
                                member.getId()
                            );
                            notification.setSessionId(session.getSessionId());
                            notification.setHostUserId(session.getHostUser().getId());
                            notification.setHostUsername(session.getHostUser().getUsername());
                            if (chatRoom != null) {
                                notification.setRoomId(chatRoom.getId());
                            }
                            notification.setSessionStatus(session.getStatus().toString());
                            notification.setTitle("Screen share started");
                            notification.setBody(session.getHostUser().getUsername() + " started sharing their screen");
                            notification.setPriority(ScreenShareNotificationMessage.PRIORITY_HIGH);
                            
                            serviceBusProducer.sendScreenShareNotification(notification);
                        }
                    }
                }
            }
            
            logger.debug("Notified screen share session started: {}", session.getSessionId());
        } catch (Exception e) {
            logger.error("Error sending session started notifications", e);
        }
    }
    
    /**
     * Notify when a screen share session ends
     */
    public void notifySessionEnded(ScreenShareSession session) {
        try {
            // Publish domain event
            if (eventGridPublisher != null) {
                ScreenShareEvent.ScreenShareEventData eventData = new ScreenShareEvent.ScreenShareEventData();
                eventData.setSessionId(session.getSessionId());
                eventData.setHostUserId(session.getHostUser().getId());
                eventData.setHostUsername(session.getHostUser().getUsername());
                eventData.setSessionStatus(session.getStatus().toString());
                eventData.setParticipantCount(session.getCurrentParticipants());
                
                ScreenShareEvent event = new ScreenShareEvent(
                    "screenshare/session/" + session.getSessionId(),
                    ScreenShareEvent.SESSION_ENDED,
                    eventData
                );
                eventGridPublisher.publishScreenShareEvent(event);
            }
            
            // Notify all participants
            if (serviceBusProducer != null && session.getParticipants() != null) {
                for (User participant : session.getParticipants()) {
                    ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                        ScreenShareNotificationMessage.TYPE_SESSION_ENDED,
                        participant.getId()
                    );
                    notification.setSessionId(session.getSessionId());
                    notification.setHostUserId(session.getHostUser().getId());
                    notification.setHostUsername(session.getHostUser().getUsername());
                    notification.setSessionStatus(session.getStatus().toString());
                    notification.setTitle("Screen share ended");
                    notification.setBody(session.getHostUser().getUsername() + " stopped sharing their screen");
                    
                    serviceBusProducer.sendScreenShareNotification(notification);
                }
            }
            
            logger.debug("Notified screen share session ended: {}", session.getSessionId());
        } catch (Exception e) {
            logger.error("Error sending session ended notifications", e);
        }
    }
    
    /**
     * Notify when a participant joins a screen share session
     */
    public void notifyParticipantJoined(ScreenShareSession session, User participant) {
        try {
            // Publish domain event
            if (eventGridPublisher != null) {
                ScreenShareEvent.ScreenShareEventData eventData = new ScreenShareEvent.ScreenShareEventData();
                eventData.setSessionId(session.getSessionId());
                eventData.setHostUserId(session.getHostUser().getId());
                eventData.setHostUsername(session.getHostUser().getUsername());
                eventData.setParticipantUserId(participant.getId());
                eventData.setParticipantUsername(participant.getUsername());
                eventData.setParticipantCount(session.getCurrentParticipants());
                
                ScreenShareEvent event = new ScreenShareEvent(
                    "screenshare/session/" + session.getSessionId(),
                    ScreenShareEvent.PARTICIPANT_JOINED,
                    eventData
                );
                eventGridPublisher.publishScreenShareEvent(event);
            }
            
            // Notify the host
            if (serviceBusProducer != null) {
                ScreenShareNotificationMessage notification = new ScreenShareNotificationMessage(
                    ScreenShareNotificationMessage.TYPE_PARTICIPANT_JOINED,
                    session.getHostUser().getId()
                );
                notification.setSessionId(session.getSessionId());
                notification.setHostUserId(session.getHostUser().getId());
                notification.setHostUsername(session.getHostUser().getUsername());
                notification.setParticipantCount(session.getCurrentParticipants());
                notification.setTitle("New viewer");
                notification.setBody(participant.getUsername() + " joined your screen share");
                notification.setPriority(ScreenShareNotificationMessage.PRIORITY_LOW);
                
                serviceBusProducer.sendScreenShareNotification(notification);
            }
            
            logger.debug("Notified participant {} joined session {}", 
                participant.getUsername(), session.getSessionId());
        } catch (Exception e) {
            logger.error("Error sending participant joined notifications", e);
        }
    }
    
    // ===== HELPER METHODS =====
    
    private ChatEvent.ChatEventData createChatEventData(ChatMessage message, ChatRoom chatRoom) {
        ChatEvent.ChatEventData eventData = new ChatEvent.ChatEventData();
        eventData.setRoomId(chatRoom.getId());
        eventData.setUserId(message.getSender().getId());
        eventData.setUsername(message.getSender().getUsername());
        eventData.setMessageId(message.getId());
        eventData.setMessageContent(truncate(message.getContent(), 200));
        eventData.setMessageType(message.getMessageType().toString());
        eventData.setRoomName(chatRoom.getName());
        eventData.setRoomType(chatRoom.getRoomType().toString());
        return eventData;
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
