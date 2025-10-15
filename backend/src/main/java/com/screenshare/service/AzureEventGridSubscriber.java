package com.screenshare.service;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationEventData;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.HelloWorldEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        
        // Handle custom HelloWorld events
        if (event.getEventType().startsWith("HelloWorld")) {
            handleHelloWorldEvent(event);
            return;
        }
        
        // Log other event types
        logger.info("Received event data: {}", event.getData().toString());
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
     * Handle custom HelloWorld events
     */
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
