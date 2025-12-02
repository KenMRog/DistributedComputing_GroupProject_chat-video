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

  const username = user?.username || user?.name || 'Anonymous';
  const chatTopic = `/topic/chat/${chatRoom.id}`;
  const signalTopic = `/topic/signal/${chatRoom.id}`;
  const screenshareTopic = `/topic/screenshare/${chatRoom.id}`; // Room-specific screen share topic

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
    if (!connected) return;

    const subs = [];
    subs.push(
      subscribe(chatTopic, (msg) => {
        const received = JSON.parse(msg.body);
        setMessages((prev) => [...prev, received]);
      })
    );

    // Subscribe to room-specific screen share events
    subs.push(
      subscribe(screenshareTopic, (msg) => {
        const data = JSON.parse(msg.body);
        if (data.userId !== user.id) {
          console.log('Received screen share event:', data);
          if (data.action === 'start') {
            // User started sharing - we'll receive their offer via signal topic
            // Add them to active shares list (stream will be added when peer connection is established)
            setActiveShares(prev => {
              const exists = prev.find(s => s.userId === data.userId);
              if (!exists) {
                const member = chatRoom.members?.find(m => m.id === data.userId);
                return [...prev, {
                  userId: data.userId,
                  username: data.username || member?.username,
                  displayName: member?.displayName || member?.username || `User ${data.userId}`,
                  stream: null, // Will be set when peer connection is established
                }];
              }
              return prev;
            });
          } else if (data.action === 'stop') {
            // User stopped sharing
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
          }
        }
      })
    );

    setIsConnected(true);
    return () => subs.forEach((s) => s.unsubscribe());
  }, [connected, chatRoom.id]);

  // Send chat message
  const handleSendMessage = (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;
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
      const stream = await navigator.mediaDevices.getDisplayMedia({ video: true, audio: true });
      setLocalStream(stream);
      setIsScreensharing(true);
      setScreenshareDialog(true);

      localVideoRef.current.srcObject = stream;

      // Notify via room-specific endpoint
      sendMessage(`/app/screenshare/${chatRoom.id}/start`, { userId: user.id, username, roomId: chatRoom.id });
      
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
        // Update active shares with the stream
        setActiveShares(prev => prev.map(share => 
          share.userId === user.id 
            ? { ...share, stream: remoteStream }
            : share
        ));
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
    } catch (err) {
      console.error('Error starting screen share:', err);
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
    if (!connected) return;
    const sub = subscribe(signalTopic, (msg) => {
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
              ? { ...share, stream: remoteStream }
              : share
          ));
        });

        responder.on('error', (err) => {
          console.error('❌ Peer connection error:', err);
        });

        responder.on('close', () => {
          console.log('🔌 Peer connection closed for user', body.fromUserId);
          delete peersRef.current[body.fromUserId];
        });

        responder.signal(body.signal);
        peersRef.current[body.fromUserId] = responder;
        
        // Update active share with peer reference
        setActiveShares(prev => prev.map(share => 
          share.userId === body.fromUserId 
            ? { ...share, peer: responder }
            : share
        ));
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
    });

    return () => sub.unsubscribe();
  }, [connected, chatRoom.id, peer]);

  // Check if any screens are being shared (for view toggle button)
  const hasActiveShares = activeShares.length > 0 || isScreensharing;
  
  // Switch to screen share view when someone starts sharing
  useEffect(() => {
    if (hasActiveShares && viewMode === 'chat' && activeShares.length > 0) {
      // Auto-switch to screen share view when first remote share is detected
      // (but only if user isn't already sharing)
      if (!isScreensharing) {
        setViewMode('screenshare');
      }
    }
  }, [activeShares.length, hasActiveShares]);

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
            {chatRoom?.members?.length === 2 && (
              <IconButton
                color={isScreensharing ? 'error' : 'primary'}
                onClick={isScreensharing ? stopScreenShare : startScreenShare}
                title={isScreensharing ? 'Stop Screen Share' : 'Start Screen Share'}
                sx={{ color: 'white' }}
              >
                {isScreensharing ? <StopIcon /> : <ScreenShareIcon />}
              </IconButton>
            )}
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

      {/* Screen Share Dialog */}
      <Dialog open={screenshareDialog} onClose={stopScreenShare} maxWidth="lg" fullWidth>
        <DialogTitle>
          <Typography variant="h6">Screen Share</Typography>
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2}>
            {isScreensharing && (
              <Grid item xs={12} md={remoteStream ? 6 : 12}>
                <Card>
                  <CardContent>
                    <Typography>Your Screen</Typography>
                    <Box sx={{ bgcolor: 'black', height: 300, borderRadius: 1 }}>
                      <video ref={localVideoRef} autoPlay muted style={{ width: '100%', height: '100%' }} />
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            )}
            {remoteStream && (
              <Grid item xs={12} md={isScreensharing ? 6 : 12}>
                <Card>
                  <CardContent>
                    <Typography>{otherUser?.displayName || 'User'}'s Screen</Typography>
                    <Box sx={{ bgcolor: 'black', height: 300, borderRadius: 1 }}>
                      <video 
                        ref={(el) => {
                          remoteVideoRef.current = el;
                          if (el && remoteStream && !el.srcObject) {
                            el.srcObject = remoteStream;
                          }
                        }} 
                        autoPlay 
                        playsInline
                        style={{ width: '100%', height: '100%', objectFit: 'contain' }} 
                      />
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            )}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={stopScreenShare} color="error" variant="contained">
            Stop Sharing
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ChatComponent;