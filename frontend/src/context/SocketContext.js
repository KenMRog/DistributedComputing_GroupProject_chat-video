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
    
    client.debug = (str) => {
      console.log('STOMP: ' + str);
    };

    client.connect({}, () => {
      setStompClient(client);
      setConnected(true);
      console.log('Connected to WebSocket');
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
