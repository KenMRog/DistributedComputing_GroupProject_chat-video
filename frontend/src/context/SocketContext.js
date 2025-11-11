import React, { createContext, useContext, useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

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
    const socket = new SockJS('http://localhost:8080/api/ws');
    const client = Stomp.over(socket);
    
    // Filter or simplify verbose library debug logs (some implementations log 'connected to server undefined')
    client.debug = (str) => {
      // suppress noisy 'connected to server undefined' messages or simplify them
      if (typeof str === 'string' && str.includes('connected to server')) {
        // try to print a clearer message with the underlying transport URL when available
        const serverUrl = socket && socket._transport && socket._transport.url ? socket._transport.url : 'unknown';
        console.log(`STOMP: connected to server ${serverUrl}`);
      } else {
        console.log('STOMP: ' + str);
      }
    };

    client.connect({}, () => {
      setStompClient(client);
      setConnected(true);
      // show resolved transport URL (SockJS transport exposes _transport.url)
      const serverUrl = socket && socket._transport && socket._transport.url ? socket._transport.url : 'unknown';
      console.log('Connected to WebSocket, server:', serverUrl);
    }, (error) => {
      console.error('WebSocket connection error:', error);
      setConnected(false);
    });

    return () => {
      if (client && client.connected) {
        client.disconnect();
      }
    };
  }, []);

  const sendMessage = (destination, body) => {
    if (stompClient && stompClient.connected) {
      stompClient.send(destination, {}, JSON.stringify(body));
    } else {
      console.error('WebSocket not connected');
    }
  };

  const subscribe = (destination, callback) => {
    if (stompClient && stompClient.connected) {
      return stompClient.subscribe(destination, callback);
    }
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
