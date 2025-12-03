package com.screenshare.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * WebSocket Interceptor to extract username from connection headers
 * and set it as Principal for user-specific routing
 */
@Component
public class WebSocketInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                // Try to get username from headers
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    // Check for username in native headers
                    Map<String, List<String>> nativeHeaders = accessor.toNativeHeaderMap();
                    if (nativeHeaders != null) {
                        List<String> usernameList = nativeHeaders.get("username");
                        if (usernameList != null && !usernameList.isEmpty()) {
                            String username = usernameList.get(0);
                            sessionAttributes.put("username", username);
                            String sessionId = accessor.getSessionId();
                            if (sessionId != null) {
                                WebSocketEventListener.registerUser(username, sessionId);
                                // Set Principal for user destination routing
                                accessor.setUser(new SimplePrincipal(username));
                            }
                        }
                    }
                }
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                // When subscribing to user-specific destination, extract username and set principal
                String destination = accessor.getDestination();
                if (destination != null && destination.startsWith("/user/")) {
                    // Extract username from destination: /user/{username}/queue/...
                    String[] parts = destination.split("/");
                    if (parts.length >= 3) {
                        String username = parts[2];
                        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                        if (sessionAttributes != null) {
                            sessionAttributes.put("username", username);
                        }
                        String sessionId = accessor.getSessionId();
                        if (sessionId != null) {
                            WebSocketEventListener.registerUser(username, sessionId);
                        }
                        // Set Principal for user destination routing
                        if (accessor.getUser() == null) {
                            accessor.setUser(new SimplePrincipal(username));
                        }
                    }
                } else {
                    // Check if we already have username in session attributes
                    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                    if (sessionAttributes != null && sessionAttributes.containsKey("username")) {
                        String username = (String) sessionAttributes.get("username");
                        if (accessor.getUser() == null) {
                            accessor.setUser(new SimplePrincipal(username));
                        }
                    }
                }
            }
        }
        
        return message;
    }

    /**
     * Simple Principal implementation for username-based routing
     */
    private static class SimplePrincipal implements Principal {
        private final String name;

        public SimplePrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}

