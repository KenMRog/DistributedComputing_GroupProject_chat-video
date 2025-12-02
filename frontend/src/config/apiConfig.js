/**
 * API Configuration
 * Centralized configuration for API and WebSocket URLs
 * Supports environment variables for different deployment scenarios
 * 
 * Priority:
 * 1. REACT_APP_API_URL environment variable (set in docker-compose or .env)
 * 2. Auto-detect from hostname (for same-machine access)
 * 3. Fallback to localhost
 */

// Get API base URL from environment variable or use default
const getApiBaseUrl = () => {
  // Priority 1: Check for environment variable (set in docker-compose or .env)
  // This is the recommended way for cross-device access
  // Example: REACT_APP_API_URL=http://192.168.1.100:8080/api
  if (process.env.REACT_APP_API_URL) {
    return process.env.REACT_APP_API_URL;
  }
  
  // Priority 2: Auto-detect from current hostname
  // This works when frontend and backend are on the same machine
  const hostname = window.location.hostname;
  const protocol = window.location.protocol;
  
  // If running on localhost, use localhost for backend
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:8080/api';
  }
  
  // For remote access (different machine), use the same hostname but port 8080
  // This assumes backend is accessible on the same hostname
  // NOTE: For cross-device access, you MUST set REACT_APP_API_URL environment variable
  // to the server's IP address (e.g., http://192.168.1.100:8080/api)
  return `${protocol}//${hostname}:8080/api`;
};

// Get WebSocket URL
// SockJS uses HTTP/HTTPS for the handshake, not raw WebSocket
// So we return the HTTP URL which SockJS will convert internally
const getWebSocketUrl = () => {
  const apiBaseUrl = getApiBaseUrl();
  
  // SockJS expects HTTP/HTTPS URL, not ws:// or wss://
  // It will handle the WebSocket upgrade internally
  // Just replace /api with /api/ws
  if (apiBaseUrl.endsWith('/api')) {
    return apiBaseUrl + '/ws';
  }
  
  // Fallback
  return 'http://localhost:8080/api/ws';
};

// Export constants
export const API_BASE_URL = getApiBaseUrl();
export const WS_URL = getWebSocketUrl();

// Helper function to build full API endpoint URLs
export const buildApiUrl = (endpoint) => {
  // Remove leading slash if present to avoid double slashes
  const cleanEndpoint = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint;
  return `${API_BASE_URL}/${cleanEndpoint}`;
};

// Log configuration in development (helps with debugging)
if (process.env.NODE_ENV === 'development') {
  console.log('🔧 API Configuration:', {
    API_BASE_URL,
    WS_URL,
    hostname: window.location.hostname,
    origin: window.location.origin,
    envVar: process.env.REACT_APP_API_URL || 'not set'
  });
}

