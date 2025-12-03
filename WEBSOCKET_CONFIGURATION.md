# WebSocket Server Configuration Explanation

## Current Setup

### Backend Configuration

1. **Context Path** (`application.yml`):
   ```yaml
   server:
     servlet:
       context-path: /api
   ```
   This means ALL endpoints are prefixed with `/api`

2. **WebSocket Endpoint** (`WebSocketConfig.java`):
   ```java
   registry.addEndpoint("/ws")
           .setAllowedOriginPatterns("*")
           .withSockJS();
   ```
   - Registered as `/ws`
   - With context path, becomes: `/api/ws` ✅
   - Uses SockJS for fallback support

3. **Message Broker Configuration**:
   ```java
   config.enableSimpleBroker("/topic");  // For receiving messages
   config.setApplicationDestinationPrefixes("/app");  // For sending messages
   ```

### Frontend Configuration

1. **Connection URL** (`SocketContext.js`):
   ```javascript
   const socket = new SockJS('http://localhost:8080/api/ws');
   ```
   ✅ Matches backend endpoint: `/api/ws`

2. **Sending Messages**:
   ```javascript
   sendMessage(`/app/chat/${chatRoom.id}/sendMessage`, message);
   ```
   ✅ Matches backend: `@MessageMapping("/chat/{roomId}/sendMessage")`

3. **Subscribing to Topics**:
   ```javascript
   subscribe(`/topic/chat/${chatRoom.id}`, callback);
   ```
   ✅ Matches backend: `messagingTemplate.convertAndSend("/topic/chat/" + roomId, message)`

## How It Works

### Message Flow

1. **Client Sends Message**:
   ```
   Frontend: sendMessage("/app/chat/123/sendMessage", {...})
   ↓
   Backend: @MessageMapping("/chat/{roomId}/sendMessage")
   ↓
   Backend: messagingTemplate.convertAndSend("/topic/chat/123", message)
   ↓
   Frontend: subscribe("/topic/chat/123", callback) receives it
   ```

2. **Screen Share Events**:
   ```
   Frontend: sendMessage("/app/screenshare/123/start", {...})
   ↓
   Backend: @MessageMapping("/screenshare/{roomId}/start")
   ↓
   Backend: messagingTemplate.convertAndSend("/topic/screenshare/123", message)
   ↓
   Frontend: subscribe("/topic/screenshare/123", callback) receives it
   ```

## Backend Endpoints Summary

### Chat Messages
- **Send**: `/app/chat/{roomId}/sendMessage` → `ChatController.sendMessageToRoom()`
- **Receive**: `/topic/chat/{roomId}` → Broadcast to all subscribers

### Screen Share
- **Start**: `/app/screenshare/{roomId}/start` → `ChatController.startScreenShare()`
- **Stop**: `/app/screenshare/{roomId}/stop` → `ChatController.stopScreenShare()`
- **Receive**: `/topic/screenshare/{roomId}` → Broadcast to all subscribers

### WebRTC Signaling
- **Send/Receive**: `/app/signal/{roomId}` → `ChatController.relaySignal()`
- **Broadcast**: `/topic/signal/{roomId}` → Relay to all subscribers

## No Changes Needed!

The current backend configuration is **correct** and matches the frontend. The setup works because:

1. ✅ WebSocket endpoint `/ws` + context path `/api` = `/api/ws` (matches frontend)
2. ✅ Message broker prefix `/app` matches frontend send destinations
3. ✅ Topic prefix `/topic` matches frontend subscriptions
4. ✅ All `@MessageMapping` paths match frontend send destinations

## Testing the Connection

To verify everything works:

1. **Check Backend Logs**:
   - Should see WebSocket connection attempts
   - Should see STOMP subscription messages

2. **Check Frontend Console**:
   - Should see: "Connected to WebSocket, server: ..."
   - Should see subscription confirmations

3. **Test Message Flow**:
   - Send a chat message
   - Check if it appears in both clients
   - Check backend logs for message processing

## Potential Issues

If messages aren't working, check:

1. **CORS**: Already configured with `setAllowedOriginPatterns("*")` ✅
2. **Security**: SecurityConfig allows all requests ✅
3. **Context Path**: Make sure frontend uses `/api/ws` (not just `/ws`) ✅
4. **Connection Timing**: Frontend waits for `connected` state before subscribing ✅

## Summary

**The backend does NOT need to be updated.** The configuration is correct and matches the frontend implementation. If you're experiencing issues, they're likely related to:
- Network connectivity
- Backend not running
- Frontend connection timing
- Message format mismatches

