import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Paper,
  TextField,
  Button,
  List,
  ListItem,
  Typography,
  Avatar,
  Grid,
  IconButton,
  Chip,
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
import { useTheme } from '../context/ThemeContext';
import { useScreenShare } from '../hooks/useScreenShare';
import ScreenShareView from './ScreenShareView';

const ChatComponent = ({ chatRoom }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [viewMode, setViewMode] = useState('chat'); // 'chat' or 'screenshare'
  const [activeShares, setActiveShares] = useState([]); // Array of { userId, username, displayName, stream, peer }
  const [notification, setNotification] = useState({ open: false, message: '', severity: 'info' });
  const messagesEndRef = useRef(null);
  
  const { connected, sendMessage, subscribe } = useSocket();
  const { user } = useAuth();
  const { theme } = useTheme();
  
  // Use screen share hook
  const {
    isScreensharing,
    localStream,
    startScreenShare: startScreenShareHook,
    stopScreenShare,
    sharingRoomIdRef,
    localStreamRef,
  } = useScreenShare(chatRoom, activeShares, setActiveShares);

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

  // Clean up peer connections when switching away from a chat (but preserve if sharing)
  const previousRoomIdRef = useRef(null);
  
  useEffect(() => {
    const previousRoomId = previousRoomIdRef.current;
    const currentRoomId = chatRoom?.id;
    previousRoomIdRef.current = currentRoomId;
    
    // When returning to a room we're sharing to, restore our own share in activeShares
    const streamToUse = localStream || localStreamRef.current;
    
    if (streamToUse && sharingRoomIdRef.current === currentRoomId && currentRoomId) {
      setActiveShares(prev => {
        const exists = prev.find(s => s.userId === user.id);
        if (!exists) {
          return [...prev, {
            userId: user.id,
            username: username,
            displayName: user.name || username,
            stream: streamToUse,
          }];
        } else if (exists && exists.stream !== streamToUse) {
          return prev.map(s => 
            s.userId === user.id 
              ? { ...s, stream: streamToUse }
              : s
          );
        }
        return prev;
      });
    }
    
    return () => {
      // Clean up remote shares when leaving a room
      const isSharingToThisRoom = sharingRoomIdRef.current === previousRoomId;
      if (!isSharingToThisRoom && previousRoomId) {
        setActiveShares(prev => prev.filter(s => s.userId === user.id));
      }
    };
  }, [chatRoom?.id, localStream, user.id, username, sharingRoomIdRef, localStreamRef]);

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

    // Screen share events are handled in the hook
    // But we still need to show notifications
    const screenshareTopic = chatRoom?.id ? `/topic/screenshare/${chatRoom.id}` : null;
    if (screenshareTopic) {
      const screenShareSub = subscribe(screenshareTopic, (msg) => {
        try {
          const data = JSON.parse(msg.body);
          if (data.userId !== user.id) {
            const member = chatRoom.members?.find(m => m.id === data.userId);
            const displayName = data.username || member?.displayName || member?.username || `User ${data.userId}`;
            
            if (data.action === 'start') {
              setNotification({
                open: true,
                message: `${displayName} started sharing their screen`,
                severity: 'info'
              });
            } else if (data.action === 'stop') {
              // Post chat message
              if (connected && chatRoom?.id) {
                const streamEndMessage = {
                  content: `${displayName}'s stream has ended.`,
                  sender: 'System',
                  senderId: null,
                  type: 'SYSTEM',
                };
                setMessages((prev) => [...prev, streamEndMessage]);
              }
              setNotification({
                open: true,
                message: `${displayName}'s stream has ended`,
                severity: 'info'
              });
            }
          }
        } catch (err) {
          console.error('Error parsing screen share message:', err);
        }
      });
      if (screenShareSub) subs.push(screenShareSub);
    }

    setIsConnected(true);
    
    return () => {
      subs.forEach((s) => {
        if (s && s.unsubscribe) {
          s.unsubscribe();
        }
      });
    };
  }, [connected, chatRoom.id, user.id, subscribe, sendMessage]);

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

  // Wrapper for startScreenShare to handle errors and notifications
  const startScreenShare = async () => {
    try {
      await startScreenShareHook();
      setViewMode('screenshare');
    } catch (err) {
      if (err.message) {
        setNotification({
          open: true,
          message: err.message,
          severity: 'warning'
        });
      } else if (err.name !== 'NotAllowedError' && err.name !== 'AbortError') {
        setNotification({
          open: true,
          message: 'Failed to start screen sharing. Please check your browser permissions.',
          severity: 'error'
        });
      }
    }
  };

  // Check if any screens are being shared (for view toggle button)
  const actuallySharing = isScreensharing || (localStreamRef.current && sharingRoomIdRef.current);
  const hasActiveShares = activeShares.some(s => 
    s.userId === user.id || (s.stream !== null && s.userId !== user.id)
  ) || actuallySharing;
  
  // Show indicator if user is sharing to a different room
  const isSharingToDifferentRoom = actuallySharing && sharingRoomIdRef.current && sharingRoomIdRef.current !== chatRoom?.id;

  // Get other user (for private 1:1)
  const getOtherUser = () =>
    chatRoom.members?.length === 2 ? chatRoom.members.find((m) => m.id !== user.id) : null;

  const otherUser = getOtherUser();

  // --- UI ---
  // Show screen share view if active and view mode is set
  // Use ref if state was lost
  const streamForView = localStream || localStreamRef.current;
  if (viewMode === 'screenshare' && hasActiveShares) {
    return (
      <ScreenShareView
        activeShares={activeShares}
        localStream={streamForView}
        onBackToChat={() => setViewMode('chat')}
        currentUser={user}
        chatRoom={chatRoom}
      />
    );
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: theme.palette.background.default }}>
      {/* Header */}
      <Paper sx={{ p: 2, mb: 2, borderRadius: 0, bgcolor: theme.palette.background.paper, color: theme.palette.text.primary }}>
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
            <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
              {otherUser?.username || chatRoom?.description}
            </Typography>
          </Grid>
          <Grid item>
            {hasActiveShares && (
              <IconButton
                onClick={() => setViewMode(viewMode === 'chat' ? 'screenshare' : 'chat')}
                title={viewMode === 'chat' ? 'View Screen Shares' : 'Back to Chat'}
                sx={{ color: theme.palette.text.primary, mr: 1 }}
              >
                <VideocamIcon />
              </IconButton>
            )}
            {isSharingToDifferentRoom && (
              <Chip 
                label={`Sharing to another chat`}
                color="warning"
                size="small"
                sx={{ mr: 1 }}
              />
            )}
            <IconButton
              color={actuallySharing ? 'error' : 'primary'}
              onClick={actuallySharing ? stopScreenShare : startScreenShare}
              title={
                actuallySharing 
                  ? isSharingToDifferentRoom 
                    ? 'You are sharing to another chat. Click to stop sharing.' 
                    : 'Stop Screen Share'
                  : 'Start Screen Share - You can choose to share your entire screen or a specific application window'
              }
              sx={{ color: theme.palette.text.primary }}
            >
              {actuallySharing ? <StopIcon /> : <ScreenShareIcon />}
            </IconButton>
          </Grid>
        </Grid>
      </Paper>

      {/* Messages */}
      <Paper sx={{ flexGrow: 1, mb: 2, overflowY: 'auto', p: 2, bgcolor: theme.palette.background.default }}>
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
                    <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                      {msg.sender}
                    </Typography>
                  )}
                  <Paper
                    sx={{
                      p: 1.2,
                      mt: 0.5,
                      bgcolor: isOwn ? theme.palette.primary.main : theme.palette.mode === 'dark' ? '#444' : '#e0e0e0',
                      color: isOwn ? 'white' : theme.palette.text.primary,
                      borderRadius: isOwn ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
                    }}
                  >
                    {msg.content}
                  </Paper>
                  <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
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
      <Paper component="form" onSubmit={handleSendMessage} sx={{ p: 2, bgcolor: theme.palette.background.paper }}>
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
                bgcolor: theme.palette.mode === 'dark' ? '#444' : '#f5f5f5',
                color: theme.palette.text.primary,
                '& fieldset': {
                  borderColor: 'transparent',
                },
                '&:hover fieldset': {
                  borderColor: theme.palette.primary.main,
                },
                '&.Mui-focused fieldset': {
                  borderColor: theme.palette.primary.main,
                },
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
              bgcolor: newMessage.trim() ? theme.palette.primary.main : theme.palette.action.disabledBackground,
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