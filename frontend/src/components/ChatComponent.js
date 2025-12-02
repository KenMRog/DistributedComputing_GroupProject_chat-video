import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Paper,
  TextField,
  Button,
  List,
  ListItem,
  ListItemText,
  Typography,
  Avatar,
  Grid,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Card,
  CardContent,
  Checkbox,
  Snackbar,
  Alert,
} from '@mui/material';
import { 
  Send as SendIcon, 
  Person as PersonIcon, 
  ScreenShare as ScreenShareIcon,
  Stop as StopIcon,
  FiberManualRecord as CircleIcon,
  Videocam as VideocamIcon
} from '@mui/icons-material';
import { useSocket } from '../context/SocketContext';
import { useAuth } from '../context/AuthContext';
import SimplePeer from 'simple-peer';
import ScreenShareView from './ScreenShareView';

const ChatComponent = ({ chatRoom }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [isScreensharing, setIsScreensharing] = useState(false);
  const [screenshareDialog, setScreenshareDialog] = useState(false);
  const [viewMode, setViewMode] = useState('chat'); // 'chat' or 'screenshare'
  const [localStream, setLocalStream] = useState(null);
  const [remoteStream, setRemoteStream] = useState(null);
  const [peer, setPeer] = useState(null);
  const [activeShares, setActiveShares] = useState([]); // Array of { userId, username, displayName, stream, peer }
  const peersRef = useRef({}); // Map of userId -> peer connection
  const messagesEndRef = useRef(null);
  const localVideoRef = useRef(null);
  const remoteVideoRef = useRef(null);
  const { connected, sendMessage, subscribe } = useSocket();
  const { user } = useAuth();

  const [inviteDialogOpen, setInviteDialogOpen] = useState(false);
  const [inviteSearchTerm, setInviteSearchTerm] = useState('');
  const [inviteResults, setInviteResults] = useState([]);
  const [selectedInviteUserIds, setSelectedInviteUserIds] = useState([]);
  const [notification, setNotification] = useState({ open: false, message: '', severity: 'info' });

  const username = user?.username || user?.name || 'Anonymous';
  
  // Create topics based on current chat room - these will update when chatRoom.id changes
  const chatTopic = chatRoom?.id ? `/topic/chat/${chatRoom.id}` : null;
  const signalTopic = chatRoom?.id ? `/topic/signal/${chatRoom.id}` : null;
  const screenshareTopic = chatRoom?.id ? `/topic/screenshare/${chatRoom.id}` : null; // Room-specific screen share topic

  // Scroll to bottom of chat
  const scrollToBottom = () => messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  useEffect(scrollToBottom, [messages]);

  // Load historical messages
  useEffect(() => {
    const loadMessages = async () => {
      try {
        const response = await fetch(`http://localhost:8080/api/chat/rooms/${chatRoom.id}/messages?userId=${user.id}`);
        if (response.ok) {
          const data = await response.json();
          setMessages(
            data.map((m) => ({
              content: m.content,
              sender: m.senderDisplayName || m.senderUsername,
              senderId: m.senderId,
              timestamp: m.createdAt,
            }))
          );
        }
      } catch (err) {
        console.error('Failed to load messages', err);
      }
    };
    loadMessages();
  }, [chatRoom.id, user.id]);

  // Subscribe to chat and screen share notifications
  useEffect(() => {
    if (!connected || !subscribe || !chatRoom?.id || !chatTopic) return;

    const subs = [];
    
    // Subscribe to chat messages
    const chatSub = subscribe(chatTopic, (msg) => {
      try {
        const received = JSON.parse(msg.body);
        setMessages((prev) => [...prev, received]);
      } catch (err) {
        console.error('Error parsing chat message:', err);
      }
    });
    if (chatSub) subs.push(chatSub);

    // Subscribe to room-specific screen share events
    const screenShareSub = subscribe(screenshareTopic, (msg) => {
      try {
        const data = JSON.parse(msg.body);
        if (data.userId !== user.id) {
          console.log('Received screen share event:', data);
          if (data.action === 'start') {
            // User started sharing - we'll receive their offer via signal topic
            // Add them to active shares list (stream will be added when peer connection is established)
            const member = chatRoom.members?.find(m => m.id === data.userId);
            const displayName = data.username || member?.displayName || member?.username || `User ${data.userId}`;
            
            setActiveShares(prev => {
              const exists = prev.find(s => s.userId === data.userId);
              if (!exists) {
                return [...prev, {
                  userId: data.userId,
                  username: data.username || member?.username,
                  displayName: displayName,
                  stream: null, // Will be set when peer connection is established
                }];
              }
              return prev;
            });

            // Show notification
            setNotification({
              open: true,
              message: `${displayName} started sharing their screen`,
              severity: 'info'
            });
          } else if (data.action === 'stop') {
            // User stopped sharing
            const member = chatRoom.members?.find(m => m.id === data.userId);
            const displayName = data.username || member?.displayName || member?.username || `User ${data.userId}`;
            
            setActiveShares(prev => {
              const share = prev.find(s => s.userId === data.userId);
              if (share && share.peer) {
                share.peer.destroy();
              }
              // Clean up peer connection
              if (peersRef.current[data.userId]) {
                delete peersRef.current[data.userId];
              }
              return prev.filter(s => s.userId !== data.userId);
            });

            // Show notification
            setNotification({
              open: true,
              message: `${displayName} stopped sharing their screen`,
              severity: 'info'
            });
          }
        }
      } catch (err) {
        console.error('Error parsing screen share message:', err);
      }
    });
    if (screenShareSub) subs.push(screenShareSub);

    setIsConnected(true);
    return () => {
      subs.forEach((s) => {
        if (s && s.unsubscribe) {
          s.unsubscribe();
        }
      });
    };
  }, [connected, chatRoom.id, user.id, subscribe]);

  // Send chat message
  const handleSendMessage = (e) => {
    e.preventDefault();
    if (!newMessage.trim() || !connected || !chatRoom?.id) return;
    const msg = {
      content: newMessage,
      sender: username,
      senderId: user.id,
      type: 'CHAT',
    };
    sendMessage(`/app/chat/${chatRoom.id}/sendMessage`, msg);
    setNewMessage('');
  };

  // --- Screen Share ---
  const startScreenShare = async () => {
    try {
      // The browser will show a native picker where users can choose:
      // - Entire Screen
      // - Application Window
      // - Browser Tab
      const stream = await navigator.mediaDevices.getDisplayMedia({ 
        video: {
          cursor: 'always',
          // displaySurface can be 'monitor', 'window', or 'browser'
          // By not specifying, we let the user choose
        }, 
        audio: true 
      });
      setLocalStream(stream);
      setIsScreensharing(true);
      setScreenshareDialog(false); // Don't show dialog, use view toggle instead

      // WebRTC connection - create peer as initiator
      const newPeer = new SimplePeer({ initiator: true, trickle: false, stream });

      newPeer.on('signal', (signalData) => {
        sendMessage(`/app/signal/${chatRoom.id}`, {
          type: 'offer',
          fromUserId: user.id,
          signal: signalData,
        });
      });

      newPeer.on('stream', (remoteStream) => {
        console.log('📺 Received remote screen stream (initiator)');
        // This shouldn't happen for initiator, but handle it just in case
        setRemoteStream(remoteStream);
      });

      newPeer.on('error', (err) => {
        console.error('❌ Peer connection error (initiator):', err);
      });

      newPeer.on('close', () => {
        console.log('🔌 Peer connection closed (initiator)');
      });

      stream.getVideoTracks()[0].onended = stopScreenShare;

      setPeer(newPeer);
      peersRef.current[user.id] = newPeer;

      // Add self to active shares AFTER peer is created
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

      // Notify via room-specific endpoint
      sendMessage(`/app/screenshare/${chatRoom.id}/start`, { userId: user.id, username, roomId: chatRoom.id });
      
      // Switch to screen share view - the stream is already set and ready
      setViewMode('screenshare');
    } catch (err) {
      console.error('Error starting screen share:', err);
      if (err.name !== 'NotAllowedError' && err.name !== 'AbortError') {
        alert('Failed to start screen sharing. Please check your browser permissions.');
      }
    }
  };

  const stopScreenShare = () => {
    if (localStream) localStream.getTracks().forEach((t) => t.stop());
    if (peer) peer.destroy();
    setPeer(null);
    setLocalStream(null);
    setRemoteStream(null);
    setIsScreensharing(false);
    setScreenshareDialog(false);

    // Remove self from active shares
    setActiveShares(prev => prev.filter(s => s.userId !== user.id));
    
    // Clean up peer connection
    if (peersRef.current[user.id]) {
      delete peersRef.current[user.id];
    }

    sendMessage(`/app/screenshare/${chatRoom.id}/stop`, { userId: user.id, username, roomId: chatRoom.id });
  };

  // Handle incoming WebRTC signaling for multiple users
  useEffect(() => {
    if (!connected || !subscribe || !signalTopic) return;
    
    const sub = subscribe(signalTopic, (msg) => {
      try {
        const body = JSON.parse(msg.body);
        if (body.fromUserId === user.id) return;

        if (body.type === 'offer') {
          // Check if we already have a peer for this user
          if (peersRef.current[body.fromUserId]) {
            console.log('Peer already exists for user', body.fromUserId);
            return;
          }

          const responder = new SimplePeer({ initiator: false, trickle: false });

          responder.on('signal', (signalData) => {
            sendMessage(`/app/signal/${chatRoom.id}`, {
              type: 'answer',
              fromUserId: user.id,
              signal: signalData,
            });
          });

          responder.on('stream', (remoteStream) => {
            console.log('📺 Received remote screen stream from user', body.fromUserId);
            // Update active shares with the stream
            setActiveShares(prev => prev.map(share => 
              share.userId === body.fromUserId 
                ? { ...share, stream: remoteStream, peer: responder }
                : share
            ));
          });

          responder.on('error', (err) => {
            console.error('❌ Peer connection error:', err);
          });

          responder.on('close', () => {
            console.log('🔌 Peer connection closed for user', body.fromUserId);
            delete peersRef.current[body.fromUserId];
            // Remove from active shares
            setActiveShares(prev => prev.filter(s => s.userId !== body.fromUserId));
          });

          responder.signal(body.signal);
          peersRef.current[body.fromUserId] = responder;
          
          // Ensure the user is in active shares (they should already be from the start event)
          setActiveShares(prev => {
            const exists = prev.find(s => s.userId === body.fromUserId);
            if (!exists) {
              // This shouldn't happen, but handle it just in case
              const member = chatRoom.members?.find(m => m.id === body.fromUserId);
              return [...prev, {
                userId: body.fromUserId,
                username: member?.username,
                displayName: member?.displayName || member?.username || `User ${body.fromUserId}`,
                stream: null,
                peer: responder,
              }];
            }
            // Update with peer reference
            return prev.map(share => 
              share.userId === body.fromUserId 
                ? { ...share, peer: responder }
                : share
            );
          });
        } else if (body.type === 'answer') {
          // Handle answer for our own peer connection (when we're sharing)
          const targetPeer = peersRef.current[body.fromUserId] || peer;
          if (targetPeer) {
            targetPeer.signal(body.signal);
          } else if (peer) {
            // Fallback to main peer
            peer.signal(body.signal);
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
  }, [connected, chatRoom.id, peer, subscribe, user.id]);

  // Check if any screens are being shared (for view toggle button)
  const hasActiveShares = activeShares.length > 0 || isScreensharing;
  
  // Switch to screen share view when someone starts sharing (optional - can be removed if you don't want auto-switch)
  useEffect(() => {
    // Only auto-switch if there are remote shares (not our own)
    const remoteShares = activeShares.filter(s => s.userId !== user.id);
    if (remoteShares.length > 0 && viewMode === 'chat' && !isScreensharing) {
      // Optional: Uncomment to auto-switch to screen share view
      // setViewMode('screenshare');
    }
  }, [activeShares.length, hasActiveShares, isScreensharing, viewMode, user.id]);

  // Get other user (for private 1:1)
  const getOtherUser = () =>
    chatRoom.members?.length === 2 ? chatRoom.members.find((m) => m.id !== user.id) : null;

  const otherUser = getOtherUser();

  // --- UI ---
  // Show screen share view if active and view mode is set
  if (viewMode === 'screenshare' && hasActiveShares) {
    return (
      <ScreenShareView
        activeShares={activeShares}
        localStream={localStream}
        onBackToChat={() => setViewMode('chat')}
        currentUser={user}
        chatRoom={chatRoom}
      />
    );
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: '#1a1a1a' }}>
      {/* Header */}
      <Paper sx={{ p: 2, mb: 2, borderRadius: 0, bgcolor: '#2d2d2d', color: 'white' }}>
        <Grid container alignItems="center" spacing={2}>
          <Grid item>
            <Avatar sx={{ bgcolor: 'primary.main' }}>
              {otherUser ? (otherUser.displayName || otherUser.username).charAt(0).toUpperCase() : <PersonIcon />}
            </Avatar>
          </Grid>
          <Grid item>
            <CircleIcon
              sx={{
                color: isConnected ? 'success.main' : 'error.main',
                fontSize: 12,
                mr: 1,
              }}
            />
          </Grid>
          <Grid item xs>
            <Typography variant="h6">{otherUser?.displayName || chatRoom?.name || 'Chat'}</Typography>
            <Typography variant="body2" sx={{ color: '#ccc' }}>
              {otherUser?.username || chatRoom?.description}
            </Typography>
          </Grid>
          <Grid item>
            {hasActiveShares && (
              <IconButton
                onClick={() => setViewMode(viewMode === 'chat' ? 'screenshare' : 'chat')}
                title={viewMode === 'chat' ? 'View Screen Shares' : 'Back to Chat'}
                sx={{ color: 'white', mr: 1 }}
              >
                <VideocamIcon />
              </IconButton>
            )}
            <IconButton
              color={isScreensharing ? 'error' : 'primary'}
              onClick={isScreensharing ? stopScreenShare : startScreenShare}
              title={isScreensharing ? 'Stop Screen Share' : 'Start Screen Share - You can choose to share your entire screen or a specific application window'}
              sx={{ color: 'white' }}
            >
              {isScreensharing ? <StopIcon /> : <ScreenShareIcon />}
            </IconButton>
          </Grid>
        </Grid>
      </Paper>

      {/* Messages */}
      <Paper sx={{ flexGrow: 1, mb: 2, overflowY: 'auto', p: 2, bgcolor: '#1a1a1a' }}>
        <List>
          {messages.map((msg, i) => {
            const isOwn = msg.senderId === user.id;
            return (
              <ListItem key={i} sx={{ flexDirection: isOwn ? 'row-reverse' : 'row' }}>
                {!isOwn && (
                  <Avatar sx={{ mr: 2, bgcolor: 'secondary.main' }}>
                    {msg.sender?.charAt(0).toUpperCase()}
                  </Avatar>
                )}
                <Box sx={{ maxWidth: '70%', textAlign: isOwn ? 'right' : 'left' }}>
                  {!isOwn && (
                    <Typography variant="caption" sx={{ color: '#aaa' }}>
                      {msg.sender}
                    </Typography>
                  )}
                  <Paper
                    sx={{
                      p: 1.2,
                      mt: 0.5,
                      bgcolor: isOwn ? '#007AFF' : '#444',
                      color: 'white',
                      borderRadius: isOwn ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
                    }}
                  >
                    {msg.content}
                  </Paper>
                  <Typography variant="caption" sx={{ color: '#aaa' }}>
                    {msg.timestamp && new Date(msg.timestamp).toLocaleTimeString()}
                  </Typography>
                </Box>
              </ListItem>
            );
          })}
          <div ref={messagesEndRef} />
        </List>
      </Paper>

      {/* Message input */}
      <Paper component="form" onSubmit={handleSendMessage} sx={{ p: 2, bgcolor: '#2d2d2d' }}>
        <Box display="flex" gap={1}>
          <TextField
            fullWidth
            placeholder="iMessage"
            multiline
            maxRows={4}
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            onKeyPress={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSendMessage(e);
              }
            }}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '20px',
                bgcolor: '#444',
                color: 'white',
              },
            }}
          />
          <Button
            type="submit"
            variant="contained"
            disabled={!newMessage.trim()}
            sx={{
              borderRadius: '50%',
              width: 50,
              height: 50,
              bgcolor: newMessage.trim() ? '#007AFF' : '#666',
            }}
          >
            <SendIcon />
          </Button>
        </Box>
      </Paper>

      {/* Notification Snackbar */}
      <Snackbar
        open={notification.open}
        autoHideDuration={4000}
        onClose={() => setNotification({ ...notification, open: false })}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert 
          onClose={() => setNotification({ ...notification, open: false })} 
          severity={notification.severity}
          sx={{ width: '100%' }}
        >
          {notification.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default ChatComponent;