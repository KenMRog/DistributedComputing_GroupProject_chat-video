import React, { createContext, useContext, useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const SocketContext = createContext();

export const useSocket = () => {
  const context = useContext(SocketContext);
  if (!context) {
    throw new Error('useSocket must be used within a SocketProvider');
  }
  return context;
};

export const SocketProvider = ({ children }) => {
  const [stompClient, setStompClient] = useState(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const socketUrl = 'http://localhost:8080/ws';
    
    // Get username from localStorage
    const savedUser = localStorage.getItem('user');
    let username = null;
    if (savedUser) {
      try {
        const user = JSON.parse(savedUser);
        username = user.username || user.name || user.email;
      } catch (e) {
        console.error('Error parsing user from localStorage:', e);
      }
    }

    // Create a new STOMP client
    const client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000, // Try reconnecting every 5 seconds if disconnected
      onConnect: (frame) => {
        console.log('✅ Connected to WebSocket:', frame);
        setConnected(true);
        setStompClient(client);
        
        // Register username with backend for user-specific routing
        if (username) {
          client.publish({
            destination: '/app/screenshare.register',
            body: username
          });
          console.log('📝 Registered username:', username);
        }
      },
      onDisconnect: () => {
        console.warn('⚠️ Disconnected from WebSocket');
        setConnected(false);
        setStompClient(null);
      },
      onStompError: (frame) => {
        console.error('❌ STOMP Error:', frame.headers['message'], frame.body);
      },
      debug: (msg) => {
        // Optional: comment this line out to reduce console spam
        if (!msg.includes('PING')) console.log('STOMP Debug:', msg);
      },
    });

    client.activate();

    return () => {
      console.log('🛑 Disconnecting WebSocket...');
      client.deactivate();
    };
  }, []);

  const sendMessage = (destination, body) => {
    if (stompClient && connected) {
      stompClient.publish({ destination, body: JSON.stringify(body) });
    } else {
      console.error('❌ Cannot send message — not connected');
    }
  };

  const subscribe = (destination, callback) => {
    if (stompClient && connected) {
      return stompClient.subscribe(destination, callback);
    }
    console.warn('⚠️ Tried to subscribe before connection established');
    return null;
  };

  const value = {
    stompClient,
    connected,
    sendMessage,
    subscribe,
  };

  return (
    <SocketContext.Provider value={value}>
      {children}
    </SocketContext.Provider>
  );
};
