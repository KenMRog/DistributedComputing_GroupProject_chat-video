package com.screenshare.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Event Listener to track user sessions
 * Maps usernames to WebSocket session IDs for user-specific routing
 */
@Component
public class WebSocketEventListener {

    // Map username to session ID
    private static final Map<String, String> usernameToSession = new ConcurrentHashMap<>();
    // Map session ID to username
    private static final Map<String, String> sessionToUsername = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        System.out.println("WebSocket Connected: " + sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = sessionToUsername.remove(sessionId);
        if (username != null) {
            usernameToSession.remove(username);
            System.out.println("WebSocket Disconnected: " + sessionId + " (username: " + username + ")");
        } else {
            System.out.println("WebSocket Disconnected: " + sessionId);
        }
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        // Extract username from user-specific destination subscriptions
        if (destination != null && destination.startsWith("/user/")) {
            // Try to extract username from subscription headers or destination
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            if (sessionAttributes != null && sessionAttributes.containsKey("username")) {
                String username = (String) sessionAttributes.get("username");
                registerUser(username, sessionId);
            }
        }
    }

    /**
     * Register a username with a session ID
     */
    public static void registerUser(String username, String sessionId) {
        // Remove old session if username was already registered
        String oldSessionId = usernameToSession.remove(username);
        if (oldSessionId != null) {
            sessionToUsername.remove(oldSessionId);
        }
        
        usernameToSession.put(username, sessionId);
        sessionToUsername.put(sessionId, username);
        System.out.println("Registered user: " + username + " -> session: " + sessionId);
    }

    /**
     * Get session ID for a username
     */
    public static String getSessionId(String username) {
        return usernameToSession.get(username);
    }

    /**
     * Get username for a session ID
     */
    public static String getUsername(String sessionId) {
        return sessionToUsername.get(sessionId);
    }
}

