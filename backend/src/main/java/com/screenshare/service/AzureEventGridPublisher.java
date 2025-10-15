package com.screenshare.service;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.HelloWorldEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;

@Service
public class AzureEventGridPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureEventGridPublisher.class);
    
    private final EventGridPublisherClient publisherClient;
    private final EventGridPublisherClient topicPublisherClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AzureEventGridPublisher(
            @Qualifier("eventGridPublisherClient") EventGridPublisherClient publisherClient,
            @Qualifier("eventGridTopicPublisherClient") EventGridPublisherClient topicPublisherClient,
            ObjectMapper objectMapper) {
        this.publisherClient = publisherClient;
        this.topicPublisherClient = topicPublisherClient;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publish an event to Event Grid Domain
     */
    public void publishEvent(HelloWorldEvent helloWorldEvent) {
        if (publisherClient == null) {
            logger.error("Event Grid Publisher Client is not configured");
            throw new IllegalStateException("Azure Event Grid is not configured");
        }
        
        try {
            EventGridEvent event = new EventGridEvent(
                    helloWorldEvent.getSubject(),
                    helloWorldEvent.getEventType(),
                    BinaryData.fromObject(helloWorldEvent.getData()),
                    "1.0"
            );
            event.setId(helloWorldEvent.getId());
            event.setEventTime(OffsetDateTime.parse(helloWorldEvent.getEventTime()));
            
            publisherClient.sendEvents(Collections.singletonList(event));
            logger.info("Event published to Event Grid: {}", helloWorldEvent);
        } catch (Exception e) {
            logger.error("Error publishing event to Event Grid", e);
            throw new RuntimeException("Failed to publish event to Event Grid", e);
        }
    }
    
    /**
     * Publish an event to Event Grid Topic
     */
    public void publishEventToTopic(HelloWorldEvent helloWorldEvent) {
        if (topicPublisherClient == null) {
            logger.error("Event Grid Topic Publisher Client is not configured");
            throw new IllegalStateException("Azure Event Grid Topic is not configured");
        }
        
        try {
            EventGridEvent event = new EventGridEvent(
                    helloWorldEvent.getSubject(),
                    helloWorldEvent.getEventType(),
                    BinaryData.fromObject(helloWorldEvent.getData()),
                    "1.0"
            );
            event.setId(helloWorldEvent.getId());
            event.setEventTime(OffsetDateTime.parse(helloWorldEvent.getEventTime()));
            
            topicPublisherClient.sendEvents(Collections.singletonList(event));
            logger.info("Event published to Event Grid Topic: {}", helloWorldEvent);
        } catch (Exception e) {
            logger.error("Error publishing event to Event Grid Topic", e);
            throw new RuntimeException("Failed to publish event to Event Grid Topic", e);
        }
    }
}
