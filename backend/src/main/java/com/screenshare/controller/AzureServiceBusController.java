package com.screenshare.controller;

import com.screenshare.model.HelloWorldMessage;
import com.screenshare.service.AzureServiceBusProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/azure/servicebus")
@CrossOrigin(origins = "*")
public class AzureServiceBusController {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureServiceBusController.class);
    
    private final AzureServiceBusProducer serviceBusProducer;
    
    @Autowired
    public AzureServiceBusController(AzureServiceBusProducer serviceBusProducer) {
        this.serviceBusProducer = serviceBusProducer;
    }
    
    /**
     * Send a hello world message to the Service Bus Queue
     * GET /api/azure/servicebus/queue/hello
     */
    @GetMapping("/queue/hello")
    public ResponseEntity<Map<String, Object>> sendHelloToQueue() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            HelloWorldMessage message = new HelloWorldMessage(
                    "Hello World from Service Bus Queue!",
                    "AzureServiceBusController"
            );
            
            serviceBusProducer.sendMessageToQueue(message);
            
            response.put("success", true);
            response.put("message", "Hello World message sent to Service Bus Queue");
            response.put("data", message);
            
            logger.info("Hello World message sent to queue successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending message to queue", e);
            response.put("success", false);
            response.put("message", "Failed to send message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Send a hello world message to the Service Bus Topic
     * GET /api/azure/servicebus/topic/hello
     */
    @GetMapping("/topic/hello")
    public ResponseEntity<Map<String, Object>> sendHelloToTopic() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            HelloWorldMessage message = new HelloWorldMessage(
                    "Hello World from Service Bus Topic!",
                    "AzureServiceBusController"
            );
            
            serviceBusProducer.sendMessageToTopic(message);
            
            response.put("success", true);
            response.put("message", "Hello World message sent to Service Bus Topic");
            response.put("data", message);
            
            logger.info("Hello World message sent to topic successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending message to topic", e);
            response.put("success", false);
            response.put("message", "Failed to send message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Send a custom message to the Service Bus Queue
     * POST /api/azure/servicebus/queue/send
     */
    @PostMapping("/queue/send")
    public ResponseEntity<Map<String, Object>> sendCustomMessageToQueue(@RequestBody HelloWorldMessage message) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            serviceBusProducer.sendMessageToQueue(message);
            
            response.put("success", true);
            response.put("message", "Custom message sent to Service Bus Queue");
            response.put("data", message);
            
            logger.info("Custom message sent to queue successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending custom message to queue", e);
            response.put("success", false);
            response.put("message", "Failed to send message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Send a custom message to the Service Bus Topic
     * POST /api/azure/servicebus/topic/send
     */
    @PostMapping("/topic/send")
    public ResponseEntity<Map<String, Object>> sendCustomMessageToTopic(@RequestBody HelloWorldMessage message) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            serviceBusProducer.sendMessageToTopic(message);
            
            response.put("success", true);
            response.put("message", "Custom message sent to Service Bus Topic");
            response.put("data", message);
            
            logger.info("Custom message sent to topic successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending custom message to topic", e);
            response.put("success", false);
            response.put("message", "Failed to send message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/azure/servicebus/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Azure Service Bus");
        return ResponseEntity.ok(response);
    }
}
