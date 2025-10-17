package com.screenshare.service;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.screenshare.model.HelloWorldEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = {"azure.eventgrid.endpoint", "azure.eventgrid.key"}, matchIfMissing = false)
public class AzureEventGridPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureEventGridPublisher.class);
    
    private final EventGridPublisherClient publisherClient;
    private final EventGridPublisherClient topicPublisherClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AzureEventGridPublisher(
            @Qualifier("eventGridPublisherClient") EventGridPublisherClient publisherClient,
            @Qualifier("eventGridTopicPublisherClient") ObjectProvider<EventGridPublisherClient> topicPublisherClientProvider,
            ObjectMapper objectMapper) {
        this.publisherClient = publisherClient;
        this.topicPublisherClient = topicPublisherClientProvider.getIfAvailable();
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
            // Ensure ID and EventTime are present for robustness when receiving partial payloads
            if (helloWorldEvent.getId() == null || helloWorldEvent.getId().isBlank()) {
                helloWorldEvent.setId(UUID.randomUUID().toString());
            }
            if (helloWorldEvent.getEventTime() == null || helloWorldEvent.getEventTime().isBlank()) {
                helloWorldEvent.setEventTime(java.time.Instant.now().toString());
            }

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
            // Ensure ID and EventTime are present for robustness when receiving partial payloads
            if (helloWorldEvent.getId() == null || helloWorldEvent.getId().isBlank()) {
                helloWorldEvent.setId(UUID.randomUUID().toString());
            }
            if (helloWorldEvent.getEventTime() == null || helloWorldEvent.getEventTime().isBlank()) {
                helloWorldEvent.setEventTime(java.time.Instant.now().toString());
            }

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
