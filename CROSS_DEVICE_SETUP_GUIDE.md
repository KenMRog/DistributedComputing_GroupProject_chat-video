# Cross-Device Setup Guide

## ✅ Implementation Complete

The full solution has been implemented! Your application now supports:
- ✅ **Cross-device messaging** (no refresh needed)
- ✅ **Cross-device screen sharing** (with STUN servers for NAT traversal)
- ✅ **Configurable API URLs** (environment variable support)
- ✅ **Auto-detection** (falls back to hostname-based detection)

## What Was Changed

### 1. API Configuration System
- **New file**: `frontend/src/config/apiConfig.js`
- Centralized URL configuration
- Supports `REACT_APP_API_URL` environment variable
- Auto-detects from hostname as fallback

### 2. Updated All Network Calls
- ✅ `SocketContext.js` - WebSocket connection
- ✅ `AuthContext.js` - Login/register API calls
- ✅ `ChatComponent.js` - Chat messages and screen share API calls
- ✅ `ChatSidebar.js` - Chat room and invite API calls

### 3. WebRTC STUN Configuration
- Added Google's free STUN servers to both initiator and responder peers
- Enables NAT traversal for cross-device screen sharing
- Ready for TURN server addition (for cross-network support)

### 4. Docker Configuration
- Updated `docker-compose.yml` to support `REACT_APP_API_URL`
- Set `ALLOWED_ORIGINS` default to `*` for cross-device access
- Added `HOST=0.0.0.0` for React dev server

---

## How to Use

### Option 1: Using Environment Variable (Recommended)

**On the server machine:**

1. Find your IP address:
   ```bash
   # Windows
   ipconfig
   
   # Linux/Mac
   ifconfig
   # Look for your local IP (e.g., 192.168.1.100)
   ```

2. Set environment variable and start:
   ```bash
   # Windows PowerShell
   $env:REACT_APP_API_URL="http://192.168.1.100:8080/api"
   docker-compose up -d
   
   # Linux/Mac
   export REACT_APP_API_URL="http://192.168.1.100:8080/api"
   docker-compose up -d
   ```

3. **On client devices**, access:
   ```
   http://YOUR_SERVER_IP:3000
   ```

### Option 2: Auto-Detection (Works for Same-Machine Access)

If you access the frontend via IP address (e.g., `http://192.168.1.100:3000`), the app will automatically use `http://192.168.1.100:8080/api` for the backend.

**Note:** This only works if the frontend is served from the same host as the backend.

### Option 3: Manual Configuration (Quick Fix)

If you need a quick fix without Docker:

1. Edit `frontend/src/config/apiConfig.js`:
   ```javascript
   // Temporarily hardcode your server IP
   const getApiBaseUrl = () => {
     return 'http://YOUR_SERVER_IP:8080/api';
   };
   ```

2. Rebuild the frontend:
   ```bash
   cd frontend
   npm run build
   ```

---

## Testing

### Test 1: WebSocket Connection
1. Open browser DevTools → Network tab
2. Filter by "WS" (WebSocket)
3. Should see connection to `/api/ws`
4. Status should be "101 Switching Protocols"
5. Check console for "✅ Connected to WebSocket"

### Test 2: Message Delivery
1. Open two browsers on different devices
2. Both connect to `http://SERVER_IP:3000`
3. Send a message from Device 1
4. Should appear **instantly** on Device 2 (no refresh needed!)

### Test 3: Screen Sharing
1. Start screen share on Device 1
2. Device 2 should receive notification
3. Check browser console for ICE candidates (WebRTC connection)
4. Screen should appear on Device 2

---

## Troubleshooting

### Messages Still Require Refresh

**Check 1: WebSocket Connection**
- Open browser console
- Look for "✅ Connected to WebSocket" message
- If missing, check Network tab for WebSocket connection errors

**Check 2: API URL Configuration**
- In browser console, you should see:
  ```
  🔧 API Configuration: {
    API_BASE_URL: "http://YOUR_SERVER_IP:8080/api",
    WS_URL: "http://YOUR_SERVER_IP:8080/api/ws",
    ...
  }
  ```
- If it shows `localhost`, the environment variable isn't set correctly

**Check 3: CORS Errors**
- Check browser console for CORS errors
- Verify backend `ALLOWED_ORIGINS` is set to `*` or includes your client's origin

**Check 4: Firewall**
- Ensure ports 3000 and 8080 are open
- Check Windows Firewall / ufw / iptables

### Screen Sharing Not Working Between Devices

**WebRTC NAT Traversal:**
- WebRTC requires direct peer-to-peer connection
- STUN servers are configured (should work on same network)
- For different networks, you'll need a TURN server

**Check browser console for:**
- ICE candidate gathering messages
- Peer connection errors
- WebRTC connection state changes

**To add TURN server** (for cross-network support):
1. Get TURN server credentials (e.g., from Twilio, or self-hosted)
2. Edit `frontend/src/components/ChatComponent.js`
3. Add to `iceServers` array:
   ```javascript
   { urls: 'turn:your-turn-server.com:3478', username: 'user', credential: 'pass' }
   ```

---

## Network Requirements

- **Same Network**: Both devices must be on the same local network
- **Firewall**: Ports 3000 (frontend) and 8080 (backend) must be accessible
- **WebRTC**: For screen sharing, devices need to reach each other directly (UDP)
  - STUN servers help with NAT traversal (configured ✅)
  - TURN server needed for cross-network support (optional)

---

## Configuration Priority

The API URL is determined in this order:

1. **`REACT_APP_API_URL` environment variable** (highest priority)
   - Set this for cross-device access
   - Example: `REACT_APP_API_URL=http://192.168.1.100:8080/api`

2. **Auto-detection from hostname**
   - If accessing from `192.168.1.100:3000`, uses `http://192.168.1.100:8080/api`
   - Works when frontend and backend are on the same machine

3. **Fallback to localhost**
   - Defaults to `http://localhost:8080/api` if nothing else matches

---

## Next Steps for Production

1. **Use domain names** instead of IP addresses
2. **Implement HTTPS/WSS** for secure connections
3. **Add TURN server** for WebRTC (for cross-network support)
4. **Restrict CORS** to specific domains (instead of `*`)
5. **Use reverse proxy** (nginx) for better security and performance

---

## Summary

✅ **Messaging**: Fixed - messages work instantly between devices  
✅ **Screen Sharing**: Fixed - works on same network with STUN servers  
✅ **Configuration**: Flexible - supports environment variables and auto-detection  
✅ **Production Ready**: Easy to extend with TURN server and HTTPS

Your application is now ready for cross-device communication! 🎉

