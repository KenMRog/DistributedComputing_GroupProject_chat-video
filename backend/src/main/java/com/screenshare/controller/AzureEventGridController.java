package com.screenshare.controller;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationEventData;
import com.screenshare.model.HelloWorldEvent;
import com.screenshare.service.AzureEventGridPublisher;
import com.screenshare.service.AzureEventGridSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/azure/eventgrid")
@CrossOrigin(origins = "*")
public class AzureEventGridController {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureEventGridController.class);
    
    private final AzureEventGridPublisher eventGridPublisher;
    private final AzureEventGridSubscriber eventGridSubscriber;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AzureEventGridController(
            org.springframework.beans.factory.ObjectProvider<AzureEventGridPublisher> eventGridPublisherProvider,
            AzureEventGridSubscriber eventGridSubscriber,
            ObjectMapper objectMapper) {
        this.eventGridPublisher = eventGridPublisherProvider.getIfAvailable();
        this.eventGridSubscriber = eventGridSubscriber;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publish a hello world event to Event Grid
     * GET /api/azure/eventgrid/publish/hello
     */
    @GetMapping("/publish/hello")
    public ResponseEntity<Map<String, Object>> publishHelloEvent() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (eventGridPublisher == null) {
                response.put("success", false);
                response.put("message", "Azure Event Grid publisher is not configured");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
            HelloWorldEvent event = new HelloWorldEvent(
                    "HelloWorld.Event",
                    "/helloworld/greeting",
                    "Hello World from Event Grid!"
            );
            
            eventGridPublisher.publishEvent(event);
            
            response.put("success", true);
            response.put("message", "Hello World event published to Event Grid");
            response.put("data", event);
            
            logger.info("Hello World event published successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error publishing event", e);
            response.put("success", false);
            response.put("message", "Failed to publish event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Publish a hello world event to Event Grid Topic
     * GET /api/azure/eventgrid/topic/hello
     */
    @GetMapping("/topic/hello")
    public ResponseEntity<Map<String, Object>> publishHelloEventToTopic() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (eventGridPublisher == null) {
                response.put("success", false);
                response.put("message", "Azure Event Grid publisher is not configured");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
            HelloWorldEvent event = new HelloWorldEvent(
                    "HelloWorld.Topic.Event",
                    "/helloworld/topic/greeting",
                    "Hello World from Event Grid Topic!"
            );
            
            eventGridPublisher.publishEventToTopic(event);
            
            response.put("success", true);
            response.put("message", "Hello World event published to Event Grid Topic");
            response.put("data", event);
            
            logger.info("Hello World event published to topic successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error publishing event to topic", e);
            response.put("success", false);
            response.put("message", "Failed to publish event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Publish a custom event to Event Grid
     * POST /api/azure/eventgrid/publish
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publishCustomEvent(@RequestBody HelloWorldEvent event) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (eventGridPublisher == null) {
                response.put("success", false);
                response.put("message", "Azure Event Grid publisher is not configured");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
            eventGridPublisher.publishEvent(event);
            
            response.put("success", true);
            response.put("message", "Custom event published to Event Grid");
            response.put("data", event);
            
            logger.info("Custom event published successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error publishing custom event", e);
            response.put("success", false);
            response.put("message", "Failed to publish event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Publish a custom event to Event Grid Topic
     * POST /api/azure/eventgrid/topic/publish
     */
    @PostMapping("/topic/publish")
    public ResponseEntity<Map<String, Object>> publishCustomEventToTopic(@RequestBody HelloWorldEvent event) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (eventGridPublisher == null) {
                response.put("success", false);
                response.put("message", "Azure Event Grid publisher is not configured");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
            eventGridPublisher.publishEventToTopic(event);
            
            response.put("success", true);
            response.put("message", "Custom event published to Event Grid Topic");
            response.put("data", event);
            
            logger.info("Custom event published to topic successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error publishing custom event to topic", e);
            response.put("success", false);
            response.put("message", "Failed to publish event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Webhook endpoint for Event Grid subscription
     * POST /api/azure/eventgrid/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleEventGridWebhook(
            @RequestHeader(value = "aeg-event-type", required = false) String aegEventType,
            @RequestBody String requestBody) {
        Map<String, String> response = new HashMap<>();

        try {
            logger.info("Received Event Grid webhook. aeg-event-type={}", aegEventType);

            // Handle subscription validation handshake
            if ("SubscriptionValidation".equalsIgnoreCase(aegEventType)) {
                JsonNode root = objectMapper.readTree(requestBody);
                if (root.isArray() && root.size() > 0) {
                    JsonNode dataNode = root.get(0).path("data");
                    String validationCode = dataNode.path("validationCode").asText(null);
                    if (validationCode == null) {
                        logger.error("Validation payload missing validationCode. Payload: {}", requestBody);
                        response.put("status", "error");
                        response.put("message", "Missing validationCode in payload");
                        return ResponseEntity.badRequest().body(response);
                    }
                    logger.info("Responding to subscription validation with code: {}", validationCode);
                    Map<String, String> validationResponse = new HashMap<>();
                    validationResponse.put("validationResponse", validationCode);
                    return ResponseEntity.ok(validationResponse);
                } else {
                    logger.error("Validation payload is not an array or is empty. Payload: {}", requestBody);
                    response.put("status", "error");
                    response.put("message", "Invalid validation payload");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Normal notification events
            logger.debug("Notification payload: {}", requestBody);
            // Best-effort: parse into EventGridEvent list if possible, otherwise just acknowledge
            try {
                List<EventGridEvent> events = objectMapper.readValue(
                        requestBody,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, EventGridEvent.class)
                );
                logger.info("Parsed {} notification events", events.size());
                eventGridSubscriber.handleEvents(events);
            } catch (Exception parseEx) {
                // Do not fail the delivery if parsing fails; log and ack to avoid retries during development
                logger.warn("Failed to parse notification events to EventGridEvent. Will ack anyway. Error: {}", parseEx.getMessage());
            }

            response.put("status", "success");
            response.put("message", "Events processed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error handling webhook events", e);
            response.put("status", "error");
            response.put("message", "Failed to process events: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/azure/eventgrid/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Azure Event Grid");
        return ResponseEntity.ok(response);
    }
}
