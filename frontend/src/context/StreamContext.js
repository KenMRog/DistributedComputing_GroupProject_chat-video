import React, { createContext, useContext, useState, useRef } from 'react';

const StreamContext = createContext();

export const useStream = () => {
  const context = useContext(StreamContext);
  if (!context) {
    throw new Error('useStream must be used within a StreamProvider');
  }
  return context;
};

export const StreamProvider = ({ children }) => {
  // Track if user is currently streaming (globally across all rooms)
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamingRoomId, setStreamingRoomId] = useState(null);
  
  // Store the stop function so it can be called from anywhere
  const stopStreamRef = useRef(null);
  
  const setStreaming = (streaming, roomId = null, stopFunction = null) => {
    setIsStreaming(streaming);
    setStreamingRoomId(roomId);
    stopStreamRef.current = stopFunction;
  };
  
  const stopStreaming = () => {
    if (stopStreamRef.current) {
      stopStreamRef.current();
      stopStreamRef.current = null;
    }
    setIsStreaming(false);
    setStreamingRoomId(null);
  };
  
  return (
    <StreamContext.Provider value={{
      isStreaming,
      streamingRoomId,
      setStreaming,
      stopStreaming,
    }}>
      {children}
    </StreamContext.Provider>
  );
};

