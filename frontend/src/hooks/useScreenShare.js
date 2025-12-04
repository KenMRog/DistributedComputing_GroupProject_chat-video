import { useState, useRef, useEffect, useCallback } from 'react';
import SimplePeer from 'simple-peer';
import { useSocket } from '../context/SocketContext';
import { useStream } from '../context/StreamContext';
import { useAuth } from '../context/AuthContext';

/**
 * Custom hook to manage screen sharing functionality
 */
export const useScreenShare = (chatRoom, activeShares, setActiveShares) => {
  const { connected, sendMessage, subscribe } = useSocket();
  const { user } = useAuth();
  const { isStreaming: globalIsStreaming, streamingRoomId: globalStreamingRoomId, setStreaming } = useStream();

  const [isScreensharing, setIsScreensharing] = useState(false);
  const [localStream, setLocalStream] = useState(null);
  const [peer, setPeer] = useState(null);
  
  const peersRef = useRef({});
  const latestOfferSignalRef = useRef(null);
  const sharingRoomIdRef = useRef(null);
  const localStreamRef = useRef(null);
  const peerRef = useRef(null);

  const username = user?.username || user?.name || 'Anonymous';
  const signalTopic = chatRoom?.id ? `/topic/signal/${chatRoom.id}` : null;
  const screenshareTopic = chatRoom?.id ? `/topic/screenshare/${chatRoom.id}` : null;

  // Sync refs with state
  useEffect(() => {
    localStreamRef.current = localStream;
  }, [localStream]);
  
  useEffect(() => {
    peerRef.current = peer;
  }, [peer]);

  // Helper to get display name
  const getDisplayName = useCallback((userId, username, members) => {
    const member = members?.find(m => m.id === userId);
    return username || member?.displayName || member?.username || `User ${userId}`;
  }, []);

  // Clean up peer connection
  const cleanupPeer = useCallback((peerToClean) => {
    if (!peerToClean) return;
    
    try {
      // Clean up interruption handlers first
      if (typeof peerToClean.cleanupInterruptionHandlers === 'function') {
        try {
          peerToClean.cleanupInterruptionHandlers();
        } catch (handlerErr) {
          console.log('Error cleaning up interruption handlers:', handlerErr.message);
        }
      }
      
      // Only destroy if peer is still connected and has destroy method
      if (typeof peerToClean.destroy === 'function') {
        // Check if peer is already destroyed or destroyed
        // Some versions of simple-peer set destroyed flag
        if (peerToClean.destroyed || peerToClean._destroyed) {
          console.log('Peer already destroyed, skipping destroy call');
          return;
        }
        
        // Remove stream from peer before destroying to prevent stream errors
        try {
          if (peerToClean._pc) {
            // Remove tracks from peer connection
            const senders = peerToClean._pc.getSenders();
            senders.forEach(sender => {
              if (sender.track) {
                sender.track.stop();
              }
              if (sender.replaceTrack) {
                sender.replaceTrack(null).catch(() => {});
              }
            });
          }
          
          // Clear any stream references
          if (peerToClean.streams && Array.isArray(peerToClean.streams)) {
            peerToClean.streams.forEach(stream => {
              if (stream && stream.getTracks) {
                stream.getTracks().forEach(track => track.stop());
              }
            });
          }
        } catch (streamCleanupErr) {
          // Ignore errors during stream cleanup
          console.log('Error cleaning up peer streams:', streamCleanupErr.message);
        }
        
        try {
          // Try to end gracefully first if the method exists
          if (typeof peerToClean.end === 'function' && !peerToClean.ended) {
            peerToClean.end();
          }
        } catch (endErr) {
          // If end fails, continue to destroy
          console.log('Error ending peer:', endErr.message);
        }
        
        // Destroy the peer - wrap in additional try-catch for process/stream errors
        try {
          peerToClean.destroy();
        } catch (destroyErr) {
          // Handle various browser environment errors from simple-peer
          // The error might not have a message, so check stack trace too
          const errorMsg = destroyErr.message || String(destroyErr) || '';
          const errorStack = destroyErr.stack || '';
          const errorName = destroyErr.name || '';
          const fullError = (errorMsg + ' ' + errorStack + ' ' + errorName).toLowerCase();
          
          // Catch common simple-peer browser compatibility errors
          if (fullError.includes('process') || 
              fullError.includes('_readablestate') || 
              fullError.includes('stream is undefined') ||
              fullError.includes("can't access property") ||
              fullError.includes("cannot access property") ||
              fullError.includes('cannot read property') ||
              fullError.includes('typeerror') ||
              errorName === 'TypeError') {
            console.log('Skipping peer destroy due to browser environment limitation');
            // Manually clear references if destroy fails
            if (peerToClean.destroyed !== undefined) {
              peerToClean.destroyed = true;
            }
            if (peerToClean._destroyed !== undefined) {
              peerToClean._destroyed = true;
            }
          } else {
            console.error('Error destroying peer:', destroyErr);
          }
        }
      }
    } catch (err) {
      // Catch any unexpected errors
      console.error('Error cleaning up peer:', err);
    }
  }, []);

  // Clean up stream tracks
  const cleanupStream = useCallback((streamToClean) => {
    if (streamToClean) {
      const tracks = streamToClean.getTracks();
      tracks.forEach(t => {
        t.stop();
        console.log('Stopped track:', t.kind);
        
      });
    } else {
      console.log('No stream to clean up');
    }
  }, []);

  // Stop screen share
  const stopScreenShare = useCallback((silent = false) => {
    try {
      const roomIdToNotify = sharingRoomIdRef.current || chatRoom?.id;
      const wasStreaming = isScreensharing || localStream || localStreamRef.current;
      
      // Clear the cleanup function in StreamContext FIRST to prevent recursion
      // We'll update the context state directly instead of calling globalStopStreaming()
      setStreaming(false, null, null);
      
      // Clean up interruption handlers
      const peerToClean = peer || peerRef.current;
      cleanupPeer(peerToClean);
      
      // Clean up stream
      cleanupStream(localStream);
      cleanupStream(localStreamRef.current);
      
      // Update state
      setPeer(null);
      peerRef.current = null;
      setLocalStream(null);
      localStreamRef.current = null;
      setIsScreensharing(false);
      sharingRoomIdRef.current = null;

      // Remove self from active shares
      setActiveShares(prev => prev.filter(s => s.userId !== user.id));
      
      // Clean up peer connection ref
      if (peersRef.current[user.id]) {
        delete peersRef.current[user.id];
      }

    } catch (err) {
      console.error('Error in stopScreenShare:', err);
      // Still clean up state even if there's an error
      setStreaming(false, null, null);
      setIsScreensharing(false);
      setLocalStream(null);
      localStreamRef.current = null;
      setPeer(null);
      peerRef.current = null;
      sharingRoomIdRef.current = null;
    }
  }, [isScreensharing, localStream, peer, chatRoom, user, username, connected, sendMessage, setStreaming, setActiveShares, cleanupPeer, cleanupStream]);

  // Start screen share
  const startScreenShare = useCallback(async () => {
    try {
      // Enforce strict "one stream per user" rule
      if (globalIsStreaming && globalStreamingRoomId) {
        if (globalStreamingRoomId === chatRoom.id) {
          return; // Already sharing to this room
        } else {
          throw new Error('You are already sharing to another chatroom. Please stop sharing there first.');
        }
      }
      
      // If already sharing locally (state mismatch), stop first
      if (isScreensharing && localStream) {
        stopScreenShare(true);
        await new Promise(resolve => setTimeout(resolve, 100));
      }
      
      // Get display media stream
      const stream = await navigator.mediaDevices.getDisplayMedia({ 
        video: { cursor: 'always' }, 
        audio: true 
      });
      
      if (!stream || stream.getVideoTracks().length === 0) {
        throw new Error('Invalid stream obtained');
      }
      
      setLocalStream(stream);
      localStreamRef.current = stream;
      setIsScreensharing(true);

      // Clean up any existing peer connection
      cleanupPeer(peer);
      cleanupPeer(peersRef.current[user.id]);
      if (peersRef.current[user.id]) {
        delete peersRef.current[user.id];
      }

      // Create WebRTC peer connection as initiator
      const newPeer = new SimplePeer({ initiator: true, trickle: false, stream });

      newPeer.on('signal', (signalData) => {
        try {
          latestOfferSignalRef.current = signalData;
          sendMessage(`/app/signal/${chatRoom.id}`, {
            type: 'offer',
            fromUserId: user.id,
            signal: signalData,
          });
        } catch (err) {
          console.error('Error sending signal:', err);
        }
      });

      newPeer.on('error', (err) => {
        console.error('❌ Peer connection error (initiator):', err);
      });

      newPeer.on('close', () => {
        console.log('🔌 Peer connection closed (initiator)');
        if (isScreensharing && localStream) {
          try {
            stopScreenShare();
          } catch (err) {
            console.error('Error stopping screen share on peer close:', err);
          }
        }
      });

      // Handle track ended events
      const videoTrack = stream.getVideoTracks()[0];
      if (videoTrack) {
        videoTrack.onended = () => {
          console.log('Screen share track ended');
          stopScreenShare();
        };
        
        videoTrack.onmute = () => {
          console.log('Screen share track muted');
          setTimeout(() => {
            if (videoTrack.readyState === 'ended') {
              console.log('Screen share track ended after mute');
              stopScreenShare();
            }
          }, 1000);
        };
      }
      
      // Handle track errors
      stream.getTracks().forEach(track => {
        track.onerror = (err) => {
          console.error('Track error:', err);
          if (err.error && err.error.name === 'NotReadableError') {
            stopScreenShare();
          }
        };
      });

      // Setup interruption handlers
      const handleVisibilityChange = () => {
        if (document.hidden && videoTrack && videoTrack.readyState === 'ended') {
          stopScreenShare();
        }
      };

      const handleBeforeUnload = () => {
        // Track will end on unload, handlers will clean up
      };

      const handlePageHide = () => {
        if (videoTrack && videoTrack.readyState === 'ended') {
          stopScreenShare();
        }
      };

      document.addEventListener('visibilitychange', handleVisibilityChange);
      window.addEventListener('beforeunload', handleBeforeUnload);
      window.addEventListener('pagehide', handlePageHide);

      const cleanupInterruptionHandlers = () => {
        document.removeEventListener('visibilitychange', handleVisibilityChange);
        window.removeEventListener('beforeunload', handleBeforeUnload);
        window.removeEventListener('pagehide', handlePageHide);
      };

      newPeer.cleanupInterruptionHandlers = cleanupInterruptionHandlers;

      setPeer(newPeer);
      peerRef.current = newPeer;
      peersRef.current[user.id] = newPeer;

      // Add self to active shares
      setActiveShares(prev => {
        const exists = prev.find(s => s.userId === user.id);
        if (!exists) {
          return [...prev, {
            userId: user.id,
            username: username,
            displayName: user.name || username,
            stream: stream,
            peer: newPeer,
          }];
        }
        return prev;
      });

      sharingRoomIdRef.current = chatRoom.id;
      setStreaming(true, chatRoom.id, () => stopScreenShare(false));
      
      sendMessage(`/app/screenshare/${chatRoom.id}/start`, { 
        userId: user.id, 
        username, 
        roomId: chatRoom.id 
      });
    } catch (err) {
      console.error('Error starting screen share:', err);
      if (err.name !== 'NotAllowedError' && err.name !== 'AbortError') {
        throw err; // Let caller handle the error
      }
    }
  }, [globalIsStreaming, globalStreamingRoomId, chatRoom, isScreensharing, localStream, user, username, sendMessage, setStreaming, stopScreenShare, setActiveShares, cleanupPeer]);

  // Handle WebSocket disconnection
  useEffect(() => {
    if (!connected && (localStream || localStreamRef.current)) {
      console.log('WebSocket disconnected while streaming - stopping stream');
      // Clear context first to prevent any recursion
      setStreaming(false, null, null);
      cleanupStream(localStream);
      cleanupStream(localStreamRef.current);
      setLocalStream(null);
      localStreamRef.current = null;
      setIsScreensharing(false);
      sharingRoomIdRef.current = null;
    }
  }, [connected, localStream, setStreaming, cleanupStream]);

  // Subscribe to screen share events
  useEffect(() => {
    if (!connected || !subscribe || !screenshareTopic) return;

    const sub = subscribe(screenshareTopic, (msg) => {
      try {
        const data = JSON.parse(msg.body);
        if (data.userId === user.id) return;

        if (data.action === 'start') {
          const displayName = getDisplayName(data.userId, data.username, chatRoom.members);
          
          setActiveShares(prev => {
            const exists = prev.find(s => s.userId === data.userId);
            if (!exists) {
              return [...prev, {
                userId: data.userId,
                username: data.username,
                displayName: displayName,
                stream: null,
              }];
            }
            return prev;
          });
        } else if (data.action === 'stop') {
          const displayName = getDisplayName(data.userId, data.username, chatRoom.members);
          
          setActiveShares(prev => {
            const share = prev.find(s => s.userId === data.userId);
            
            // Clean up the peer connection
            if (share?.peer) {
              cleanupPeer(share.peer);
            }
            
            // Clean up any peer ref
            if (peersRef.current[data.userId]) {
              cleanupPeer(peersRef.current[data.userId]);
              delete peersRef.current[data.userId];
            }
            
            // Clear the stream from activeShares immediately
            return prev.filter(s => s.userId !== data.userId);
          });
        }
      } catch (err) {
        console.error('Error parsing screen share message:', err);
      }
    });

    return () => {
      if (sub && sub.unsubscribe) {
        sub.unsubscribe();
      }
    };
  }, [connected, screenshareTopic, subscribe, chatRoom, user.id, getDisplayName, setActiveShares, cleanupPeer]);

  // Handle WebRTC signaling
  useEffect(() => {
    if (!connected || !subscribe || !signalTopic) return;
    
    const sub = subscribe(signalTopic, (msg) => {
      try {
        const body = JSON.parse(msg.body);
        if (body.fromUserId === user.id) return;

        if (body.type === 'offer') {
          // Clean up existing peer if any
          if (peersRef.current[body.fromUserId]) {
            cleanupPeer(peersRef.current[body.fromUserId]);
            delete peersRef.current[body.fromUserId];
          }

          const responder = new SimplePeer({ initiator: false, trickle: false });

          responder.on('signal', (signalData) => {
            try {
              sendMessage(`/app/signal/${chatRoom.id}`, {
                type: 'answer',
                fromUserId: user.id,
                signal: signalData,
              });
            } catch (err) {
              console.error('Error sending answer signal:', err);
            }
          });

          responder.on('stream', (remoteStream) => {
            console.log('📺 Received remote screen stream from user', body.fromUserId);
            setActiveShares(prev => {
              const existingShare = prev.find(s => s.userId === body.fromUserId);
              if (existingShare) {
                return prev.map(share => 
                  share.userId === body.fromUserId 
                    ? { ...share, stream: remoteStream, peer: responder }
                    : share
                );
              } else {
                const member = chatRoom.members?.find(m => m.id === body.fromUserId);
                return [...prev, {
                  userId: body.fromUserId,
                  username: member?.username,
                  displayName: member?.displayName || member?.username || `User ${body.fromUserId}`,
                  stream: remoteStream,
                  peer: responder,
                }];
              }
            });
          });

          responder.on('error', (err) => {
            console.error('❌ Peer connection error:', err);
          });

          responder.on('close', () => {
            console.log('🔌 Peer connection closed for user', body.fromUserId);
            setActiveShares(prev => {
              const share = prev.find(s => s.userId === body.fromUserId);
              if (share?.peer) {
                cleanupPeer(share.peer);
              }
              if (peersRef.current[body.fromUserId]) {
                cleanupPeer(peersRef.current[body.fromUserId]);
                delete peersRef.current[body.fromUserId];
              }
              return prev.filter(s => s.userId !== body.fromUserId);
            });
          });

          try {
            responder.signal(body.signal);
            peersRef.current[body.fromUserId] = responder;
            
            setActiveShares(prev => {
              const exists = prev.find(s => s.userId === body.fromUserId);
              if (!exists) {
                const member = chatRoom.members?.find(m => m.id === body.fromUserId);
                return [...prev, {
                  userId: body.fromUserId,
                  username: member?.username,
                  displayName: member?.displayName || member?.username || `User ${body.fromUserId}`,
                  stream: null,
                  peer: responder,
                }];
              }
              return prev.map(share => 
                share.userId === body.fromUserId 
                  ? { ...share, peer: responder }
                  : share
              );
            });
          } catch (err) {
            console.error('Error signaling responder peer:', err);
            cleanupPeer(responder);
          }
        } else if (body.type === 'answer') {
          const targetPeer = peersRef.current[user.id] || peer;
          if (targetPeer) {
            try {
              targetPeer.signal(body.signal);
            } catch (err) {
              console.error('Error signaling peer:', err);
            }
          }
        } else if (body.type === 'request_active_shares') {
          const currentPeer = peer || peerRef.current;
          const currentStream = localStream || localStreamRef.current;
          const isCurrentlySharing = isScreensharing || (currentStream && sharingRoomIdRef.current === chatRoom.id);
          
          if (isCurrentlySharing && currentPeer && currentStream && body.fromUserId !== user.id) {
            if (latestOfferSignalRef.current) {
              try {
                sendMessage(`/app/signal/${chatRoom.id}`, {
                  type: 'offer',
                  fromUserId: user.id,
                  signal: latestOfferSignalRef.current,
                });
              } catch (err) {
                console.error('Error re-sending offer:', err);
              }
            }
          }
        }
      } catch (err) {
        console.error('Error parsing signal message:', err);
      }
    });

    return () => {
      if (sub && sub.unsubscribe) {
        sub.unsubscribe();
      }
    };
  }, [connected, signalTopic, subscribe, chatRoom, peer, localStream, isScreensharing, user.id, sendMessage, setActiveShares, cleanupPeer]);

  return {
    isScreensharing,
    localStream,
    startScreenShare,
    stopScreenShare,
    sharingRoomIdRef,
    localStreamRef,
    peerRef,
  };
};

