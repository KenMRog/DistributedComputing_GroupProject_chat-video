package com.screenshare.service;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationEventData;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.ChatEvent;
import com.screenshare.model.HelloWorldEvent;
import com.screenshare.model.ScreenShareEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling incoming Event Grid events
 * Processes chat events, screen share events, and system events
 */
@Service
public class AzureEventGridSubscriber {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureEventGridSubscriber.class);
    
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AzureEventGridSubscriber(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Handle incoming Event Grid events
     * This method would typically be called from a webhook endpoint
     */
    public void handleEvents(List<EventGridEvent> events) {
        for (EventGridEvent event : events) {
            handleEvent(event);
        }
    }
    
    /**
     * Handle a single Event Grid event
     */
    public void handleEvent(EventGridEvent event) {
        logger.info("Received Event Grid event:");
        logger.info("  Event Type: {}", event.getEventType());
        logger.info("  Subject: {}", event.getSubject());
        logger.info("  Event Time: {}", event.getEventTime());
        logger.info("  ID: {}", event.getId());
        
        // Handle subscription validation event
        if (event.getEventType().equals("Microsoft.EventGrid.SubscriptionValidationEvent")) {
            handleSubscriptionValidation(event);
            return;
        }
        
        // Handle chat events
        if (event.getEventType().startsWith("chat.")) {
            handleChatEvent(event);
            return;
        }
        
        // Handle screen share events
        if (event.getEventType().startsWith("screenshare.")) {
            handleScreenShareEvent(event);
            return;
        }
        
        // Handle custom HelloWorld events (legacy)
        if (event.getEventType().startsWith("HelloWorld")) {
            handleHelloWorldEvent(event);
            return;
        }
        
        // Log other event types
        logger.info("Received unhandled event type: {}, data: {}", 
                event.getEventType(), event.getData().toString());
    }
    
    /**
     * Handle Event Grid subscription validation
     */
    private void handleSubscriptionValidation(EventGridEvent event) {
        try {
            SubscriptionValidationEventData validationData = 
                    event.getData().toObject(SubscriptionValidationEventData.class);
            
            logger.info("Subscription validation code: {}", validationData.getValidationCode());
            
            SubscriptionValidationResponse response = new SubscriptionValidationResponse();
            response.setValidationResponse(validationData.getValidationCode());
            
            logger.info("Subscription validation response: {}", response.getValidationResponse());
        } catch (Exception e) {
            logger.error("Error handling subscription validation", e);
        }
    }
    
    /**
     * Handle chat-related events
     */
    private void handleChatEvent(EventGridEvent event) {
        try {
            String eventData = event.getData().toString();
            logger.info("Chat Event - Type: {}, Subject: {}", event.getEventType(), event.getSubject());
            logger.debug("Chat Event Data: {}", eventData);
            
            // Parse the event data
            ChatEvent.ChatEventData chatData = event.getData().toObject(ChatEvent.ChatEventData.class);
            
            // Process based on event type
            switch (event.getEventType()) {
                case ChatEvent.MESSAGE_SENT:
                    logger.info("Message sent in room {} by user {}", 
                            chatData.getRoomId(), chatData.getUsername());
                    // Could trigger analytics, logging, or external integrations
                    break;
                case ChatEvent.USER_JOINED_ROOM:
                    logger.info("User {} joined room {}", 
                            chatData.getUsername(), chatData.getRoomId());
                    break;
                case ChatEvent.INVITE_SENT:
                    logger.info("Invite sent from user {} to user {}", 
                            chatData.getUserId(), chatData.getInvitedUserId());
                    break;
                case ChatEvent.INVITE_ACCEPTED:
                    logger.info("Invite {} accepted by user {}", 
                            chatData.getInviteId(), chatData.getUsername());
                    break;
                default:
                    logger.info("Unhandled chat event type: {}", event.getEventType());
            }
            
            logger.info("Successfully processed chat event with ID: {}", event.getId());
        } catch (Exception e) {
            logger.error("Error handling chat event", e);
        }
    }
    
    /**
     * Handle screen share events
     */
    private void handleScreenShareEvent(EventGridEvent event) {
        try {
            String eventData = event.getData().toString();
            logger.info("Screen Share Event - Type: {}, Subject: {}", 
                    event.getEventType(), event.getSubject());
            logger.debug("Screen Share Event Data: {}", eventData);
            
            // Parse the event data
            ScreenShareEvent.ScreenShareEventData shareData = 
                    event.getData().toObject(ScreenShareEvent.ScreenShareEventData.class);
            
            // Process based on event type
            switch (event.getEventType()) {
                case ScreenShareEvent.SESSION_STARTED:
                    logger.info("Screen share session {} started by user {}", 
                            shareData.getSessionId(), shareData.getHostUsername());
                    break;
                case ScreenShareEvent.SESSION_ENDED:
                    logger.info("Screen share session {} ended", shareData.getSessionId());
                    break;
                case ScreenShareEvent.PARTICIPANT_JOINED:
                    logger.info("Participant {} joined session {}", 
                            shareData.getParticipantUsername(), shareData.getSessionId());
                    break;
                case ScreenShareEvent.PARTICIPANT_LEFT:
                    logger.info("Participant {} left session {}", 
                            shareData.getParticipantUsername(), shareData.getSessionId());
                    break;
                default:
                    logger.info("Unhandled screen share event type: {}", event.getEventType());
            }
            
            logger.info("Successfully processed screen share event with ID: {}", event.getId());
        } catch (Exception e) {
            logger.error("Error handling screen share event", e);
        }
    }
    
    /**
     * Handle custom HelloWorld events (Legacy support)
     * @deprecated Legacy support for HelloWorld events
     */
    @Deprecated
    private void handleHelloWorldEvent(EventGridEvent event) {
        try {
            String eventData = event.getData().toString();
            logger.info("HelloWorld Event Data: {}", eventData);
            
            // Process the event data as needed
            logger.info("Successfully processed HelloWorld event with ID: {}", event.getId());
        } catch (Exception e) {
            logger.error("Error handling HelloWorld event", e);
        }
    }
    
    /**
     * Get validation response for subscription validation
     */
    public String getValidationResponse(String validationCode) {
        try {
            SubscriptionValidationResponse response = new SubscriptionValidationResponse();
            response.setValidationResponse(validationCode);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("Error creating validation response", e);
            return null;
        }
    }
}
