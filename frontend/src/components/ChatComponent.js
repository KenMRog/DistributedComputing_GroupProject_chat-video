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
  Chip,
  Grid,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Checkbox,
} from '@mui/material';
import { 
  Send as SendIcon, 
  Person as PersonIcon, 
  ScreenShare as ScreenShareIcon,
  Stop as StopIcon,
  FiberManualRecord as CircleIcon
} from '@mui/icons-material';
import { motion, AnimatePresence } from 'framer-motion';
import { useSocket } from '../context/SocketContext';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';

const ChatComponent = ({ chatRoom }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [isScreensharing, setIsScreensharing] = useState(false);
  const [screenshareDialog, setScreenshareDialog] = useState(false);
  const [localStream, setLocalStream] = useState(null);
  const [remoteStream, setRemoteStream] = useState(null);
  const messagesEndRef = useRef(null);
  const localVideoRef = useRef(null);
  const remoteVideoRef = useRef(null);
  const { connected, sendMessage, subscribe } = useSocket();
  const { user } = useAuth();
  const { mode, theme: appTheme } = useTheme();
  
  const [inviteDialogOpen, setInviteDialogOpen] = useState(false);
  const [inviteSearchTerm, setInviteSearchTerm] = useState('');
  const [inviteResults, setInviteResults] = useState([]);
  const [selectedInviteUserIds, setSelectedInviteUserIds] = useState([]);
  
  // Get username from authenticated user
  const username = user?.username || user?.name || 'Anonymous';
  
  // Theme-aware colors
  const isLight = mode === 'light';
  const chatBackground = isLight ? appTheme.palette.background.default : '#1a1a1a';
  const chatHeaderBg = isLight ? appTheme.palette.background.paper : '#2d2d2d';
  const messageBubbleOwn = appTheme.palette.primary.main;
  const messageBubbleOther = isLight ? '#e0e0e0' : '#444444';
  const textPrimary = appTheme.palette.text.primary;
  const textSecondary = appTheme.palette.text.secondary;
  const inputBg = isLight ? appTheme.palette.background.paper : '#2d2d2d';
  const inputFieldBg = isLight ? '#f5f5f5' : '#444444';
  const inputBorder = isLight ? '#e0e0e0' : '#666666';
  const inputBorderHover = isLight ? '#b0b0b0' : '#888888';
  const sendButtonDisabled = isLight ? '#cccccc' : '#666666';

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Load historical messages when chat room changes
  useEffect(() => {
    const loadMessages = async () => {
      if (chatRoom && user) {
        try {
          const response = await fetch(`http://localhost:8080/api/chat/rooms/${chatRoom.id}/messages?userId=${user.id}`);
          if (response.ok) {
            const historicalMessages = await response.json();
            // Convert to WebSocket message format for consistency
            const formattedMessages = historicalMessages.map(msg => ({
              content: msg.content,
              sender: msg.senderDisplayName || msg.senderUsername,
              senderId: msg.senderId,
              type: msg.messageType || 'CHAT',
              timestamp: msg.createdAt
            }));
            setMessages(formattedMessages);
          }
        } catch (error) {
          console.error('Failed to load messages:', error);
          setMessages([]); // Start with empty if loading fails
        }
      }
    };

    loadMessages();
  }, [chatRoom, user]);

  useEffect(() => {
    if (connected && subscribe && username && chatRoom) {
      const chatTopic = `/topic/chat/${chatRoom.id}`;
      const screenshareTopic = `/topic/screenshare/${chatRoom.id}`;
      
      const chatSubscription = subscribe(chatTopic, (message) => {
        const receivedMessage = JSON.parse(message.body);
        setMessages(prev => [...prev, receivedMessage]);
      });

      const screenshareSubscription = subscribe(screenshareTopic, (message) => {
        const data = JSON.parse(message.body);
        if (data.userId !== user.id) {
          if (data.action === 'start') {
            setRemoteStream(data);
            setScreenshareDialog(true);
          } else if (data.action === 'stop') {
            setRemoteStream(null);
          }
        }
      });
      
      setIsConnected(true);
      
      return () => {
        if (chatSubscription) {
          chatSubscription.unsubscribe();
        }
        if (screenshareSubscription) {
          screenshareSubscription.unsubscribe();
        }
      };
    }
  }, [connected, subscribe, username, sendMessage, chatRoom, user.id]);

  const fetchUsersForInvite = async (q) => {
    try {
      const base = `http://localhost:8080/api/users`;
  const url = q && q.trim() !== '' ? `${base}?q=${encodeURIComponent(q)}&excludeActiveDmWith=${user.id}&excludeMemberOfRoom=${chatRoom?.id}` : `${base}?excludeActiveDmWith=${user.id}&excludeMemberOfRoom=${chatRoom?.id}`;
      const response = await fetch(url);
      if (response.ok) {
        const data = await response.json();
        // filter out current user if present
        setInviteResults(data.filter(u => u.id !== user.id));
      }
    } catch (err) {
      console.error('Error fetching users for invite:', err);
    }
  };

  const openInviteDialog = () => {
    setInviteSearchTerm('');
    setInviteResults([]);
    setSelectedInviteUserIds([]);
    setInviteDialogOpen(true);
  };

  // Debounced search for invite dialog
  useEffect(() => {
    let t;
    if (inviteDialogOpen) {
      t = setTimeout(() => {
        fetchUsersForInvite(inviteSearchTerm);
      }, 300);
    }
    return () => clearTimeout(t);
  }, [inviteSearchTerm, inviteDialogOpen]);

  const handleSendInvites = async () => {
    if (!chatRoom || !selectedInviteUserIds.length) return;
    try {
      const payload = { invitedUserIds: selectedInviteUserIds };
      const resp = await fetch(`http://localhost:8080/api/chat/rooms/${chatRoom.id}/invites?inviterId=${user.id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (resp.ok) {
        setInviteDialogOpen(false);
        setSelectedInviteUserIds([]);
        // Optionally refresh pending invites or chatroom
      } else {
        console.error('Failed to send invites', await resp.text());
      }
    } catch (err) {
      console.error('Error sending invites:', err);
    }
  };

  const handleSendMessage = (e) => {
    e.preventDefault();
    if (newMessage.trim() && username && chatRoom) {
      const message = {
        content: newMessage,
        sender: username,
        senderId: user.id,
        type: 'CHAT'
      };
      
      sendMessage(`/app/chat/${chatRoom.id}/sendMessage`, message);
      setNewMessage('');
    }
  };

  const startScreenShare = async () => {
    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: true,
      });

      setLocalStream(stream);
      setIsScreensharing(true);
      setScreenshareDialog(true);

      if (localVideoRef.current) {
        localVideoRef.current.srcObject = stream;
      }

      // Notify other user in the chat room about screen share
      if (connected && sendMessage && chatRoom) {
        sendMessage(`/app/screenshare/${chatRoom.id}/start`, {
          userId: user.id,
          username: username,
          roomId: chatRoom.id
        });
      }

      // Handle when user stops sharing by clicking the browser's stop button
      stream.getVideoTracks()[0].onended = () => {
        stopScreenShare();
      };
    } catch (error) {
      console.error('Error starting screen share:', error);
    }
  };

  const stopScreenShare = () => {
    if (localStream) {
      localStream.getTracks().forEach(track => track.stop());
      setLocalStream(null);
    }
    
    setIsScreensharing(false);
    setScreenshareDialog(false);

    // Notify other user about stopping screen share
    if (connected && sendMessage && chatRoom) {
      sendMessage(`/app/screenshare/${chatRoom.id}/stop`, {
        userId: user.id,
        username: username,
        roomId: chatRoom.id
      });
    }

    if (localVideoRef.current) {
      localVideoRef.current.srcObject = null;
    }
  };

  if (!isConnected) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <Typography variant="h6">Connecting to chat...</Typography>
      </Box>
    );
  }

  const getOtherUser = () => {
    if (chatRoom && chatRoom.members && chatRoom.members.length === 2) {
      return chatRoom.members.find(member => member.id !== user.id);
    }
    return null;
  };

  const otherUser = getOtherUser();

  return (
    <Box
      component={motion.div}
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.3 }}
      sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: chatBackground }}
    >
      {/* Chat Header */}
      <Paper
        component={motion.div}
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.3, delay: 0.1 }}
        sx={{ p: 2, mb: 2, borderRadius: 0, bgcolor: chatHeaderBg, color: textPrimary }}
      >
        <Grid container spacing={2} alignItems="center">
          <Grid item>
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ duration: 0.3, delay: 0.2 }}
            >
              <Avatar sx={{ bgcolor: 'primary.main' }}>
                {otherUser ? (otherUser.displayName || otherUser.username).charAt(0).toUpperCase() : <PersonIcon />}
              </Avatar>
            </motion.div>
          </Grid>
          <Grid item>
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ duration: 0.2, delay: 0.3 }}
            >
              <CircleIcon 
                sx={{ 
                  color: isConnected ? 'success.main' : 'error.main', 
                  fontSize: 12,
                  mr: 1 
                }} 
              />
            </motion.div>
          </Grid>
          <Grid item xs>
            <motion.div
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.3, delay: 0.2 }}
            >
              <Typography variant="h6" sx={{ color: textPrimary }}>
                {otherUser ? (otherUser.displayName || otherUser.username) : chatRoom?.name || 'Chat'}
              </Typography>
            </motion.div>
            <motion.div
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.3, delay: 0.25 }}
            >
              {otherUser ? (
                <Typography variant="body2" sx={{ color: textSecondary }}>
                  {otherUser ? (otherUser.username) : ''}
                </Typography>
              ) : (
                // show the chat description at the top
                <Typography variant="body2" sx={{ color: textSecondary }}>
                  {chatRoom?.description}
                </Typography>
              )}
            </motion.div>
          </Grid>
          <Grid item>
            {chatRoom?.roomType === 'PRIVATE' && user && chatRoom?.createdById === user.id && (
              <motion.div
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                style={{ display: 'inline-block' }}
              >
                <Button variant="outlined" color="inherit" onClick={openInviteDialog} sx={{ mr: 1 }}>
                  Invite
                </Button>
              </motion.div>
            )}
            <motion.div
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.95 }}
              style={{ display: 'inline-block' }}
            >
              <IconButton 
                color={isScreensharing ? "error" : "primary"}
                onClick={isScreensharing ? stopScreenShare : startScreenShare}
                title={isScreensharing ? "Stop Screen Share" : "Start Screen Share"}
                sx={{ color: textPrimary }}
              >
                {isScreensharing ? <StopIcon /> : <ScreenShareIcon />}
              </IconButton>
            </motion.div>
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ flexGrow: 1, mb: 2, overflow: 'hidden', display: 'flex', flexDirection: 'column', bgcolor: chatBackground }}>
        <List sx={{ flexGrow: 1, overflow: 'auto', p: 1 }}>
          <AnimatePresence initial={false}>
            {messages.map((message, index) => {
              const isOwnMessage = message.senderId === user.id;
              return (
                <motion.div
                  key={`${message.timestamp || index}-${message.content}`}
                  initial={{ opacity: 0, y: 10, scale: 0.95 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.95, height: 0 }}
                  transition={{ 
                    duration: 0.2,
                    delay: index === messages.length - 1 ? 0.1 : 0
                  }}
                  layout
                >
                  <ListItem 
                    alignItems="flex-start" 
                    sx={{ 
                      mb: 1, 
                      display: 'flex',
                      flexDirection: isOwnMessage ? 'row-reverse' : 'row',
                      justifyContent: isOwnMessage ? 'flex-end' : 'flex-start',
                      width: '100%'
                    }}
                  >
                {!isOwnMessage && (
                  <Avatar sx={{ mr: 2, bgcolor: 'secondary.main' }}>
                    {message.sender?.charAt(0).toUpperCase()}
                  </Avatar>
                )}
                <Box
                  sx={{
                    maxWidth: '70%',
                    minWidth: '100px',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: isOwnMessage ? 'flex-end' : 'flex-start',
                    ml: isOwnMessage ? 'auto' : 0,
                    mr: isOwnMessage ? 2 : 0
                  }}
                >
                  {!isOwnMessage && (
                    <Typography 
                      variant="caption" 
                      sx={{ mb: 0.5, ml: 1, color: textSecondary }}
                    >
                      {message.sender}
                    </Typography>
                  )}
                  <Paper
                    elevation={isLight ? 0 : 1}
                    sx={{
                      p: 1.5,
                      bgcolor: isOwnMessage ? messageBubbleOwn : messageBubbleOther,
                      color: isOwnMessage 
                        ? '#ffffff' 
                        : (isLight ? textPrimary : '#ffffff'),
                      borderRadius: isOwnMessage 
                        ? '18px 18px 4px 18px' 
                        : '18px 18px 18px 4px',
                      wordBreak: 'break-word',
                      border: isLight && !isOwnMessage ? `1px solid ${appTheme.palette.divider}` : 'none'
                    }}
                  >
                    <Typography variant="body2" sx={{ color: isOwnMessage ? '#ffffff' : (isLight ? textPrimary : '#ffffff') }}>
                      {message.content}
                    </Typography>
                  </Paper>
                  <Typography 
                    variant="caption" 
                    sx={{ 
                      mt: 0.5, 
                      alignSelf: isOwnMessage ? 'flex-end' : 'flex-start',
                      mr: isOwnMessage ? 1 : 0,
                      ml: isOwnMessage ? 0 : 1,
                      color: textSecondary
                    }}
                  >
                    {message.timestamp && new Date(message.timestamp).toLocaleTimeString()}
                  </Typography>
                </Box>
                {isOwnMessage && (
                  <Avatar sx={{ ml: 2, bgcolor: 'primary.main' }}>
                    {username?.charAt(0).toUpperCase()}
                  </Avatar>
                )}
                  </ListItem>
                </motion.div>
              );
            })}
          </AnimatePresence>
          <div ref={messagesEndRef} />
        </List>
      </Paper>

      <motion.div
        initial={{ y: 20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.3, delay: 0.2 }}
      >
        <Paper component="form" onSubmit={handleSendMessage} sx={{ p: 2, bgcolor: inputBg }}>
        <Box display="flex" gap={1} alignItems="flex-end">
          <TextField
            color="primary"
            fullWidth
            variant="outlined"
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
                bgcolor: inputFieldBg,
                color: textPrimary,
                '& fieldset': {
                  border: `1px solid ${inputBorder}`
                },
                '&:hover fieldset': {
                  border: `1px solid ${inputBorderHover}`
                },
                '&.Mui-focused fieldset': {
                  border: `2px solid ${appTheme.palette.primary.main}`
                }
              },
              '& .MuiInputBase-input': {
                color: textPrimary,
                '&::placeholder': {
                  color: textSecondary,
                  opacity: 1
                }
              }
            }}
          />
          <motion.div
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
          >
            <Button
              type="submit"
              variant="contained"
              sx={{
                minWidth: '50px',
                width: '50px',
                height: '50px',
                borderRadius: '50%',
                p: 0,
                bgcolor: newMessage.trim() ? appTheme.palette.primary.main : sendButtonDisabled,
                color: 'white',
                '&:hover': {
                  bgcolor: newMessage.trim() ? appTheme.palette.primary.dark : sendButtonDisabled
                },
                '&:disabled': {
                  bgcolor: sendButtonDisabled,
                  color: isLight ? '#999999' : '#999999'
                }
              }}
              disabled={!newMessage.trim()}
            >
              <SendIcon />
            </Button>
          </motion.div>
        </Box>
        </Paper>
      </motion.div>

      {/* Screen Share Dialog */}
      <Dialog 
        open={screenshareDialog} 
        onClose={() => setScreenshareDialog(false)}
        maxWidth="lg"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h6">Screen Share</Typography>
            <IconButton onClick={() => setScreenshareDialog(false)}>
              ✕
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2}>
            {/* Local Screen Share */}
            {isScreensharing && (
              <Grid item xs={12} md={remoteStream ? 6 : 12}>
                <Card>
                  <CardContent>
                    <Typography variant="subtitle1" gutterBottom>
                      Your Screen
                    </Typography>
                    <Box
                      sx={{
                        width: '100%',
                        height: 300,
                        bgcolor: 'black',
                        borderRadius: 1,
                        overflow: 'hidden',
                      }}
                    >
                      <video
                        ref={localVideoRef}
                        autoPlay
                        muted
                        style={{
                          width: '100%',
                          height: '100%',
                          objectFit: 'contain',
                        }}
                      />
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            )}

            {/* Remote Screen Share */}
            {remoteStream && (
              <Grid item xs={12} md={isScreensharing ? 6 : 12}>
                <Card>
                  <CardContent>
                    <Typography variant="subtitle1" gutterBottom>
                      {remoteStream.username}'s Screen
                    </Typography>
                    <Box
                      sx={{
                        width: '100%',
                        height: 300,
                        bgcolor: 'black',
                        borderRadius: 1,
                        overflow: 'hidden',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      <Typography variant="body1" color="white">
                        {remoteStream.username} is sharing their screen
                      </Typography>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            )}

            {/* No active screen share */}
            {!isScreensharing && !remoteStream && (
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Box
                      sx={{
                        textAlign: 'center',
                        p: 4,
                      }}
                    >
                      <ScreenShareIcon sx={{ fontSize: 60, color: 'grey.400', mb: 2 }} />
                      <Typography variant="h6" color="text.secondary" gutterBottom>
                        No Active Screen Share
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Click the screen share button to start sharing your screen
                      </Typography>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            )}
          </Grid>
        </DialogContent>
      </Dialog>

      {/* Invite Users Dialog */}
      <Dialog
        open={inviteDialogOpen}
        onClose={() => setInviteDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Invite Users</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 1 }}>Search for users to invite.</Typography>
          <TextField
            fullWidth
            placeholder="Search users by username or email"
            value={inviteSearchTerm}
            onChange={(e) => setInviteSearchTerm(e.target.value)}
            sx={{ mb: 1 }}
          />
          <List>
            {inviteResults.map((u) => (
              <ListItem
                key={u.id}
                button
                onClick={() => {
                  const exists = selectedInviteUserIds.includes(u.id);
                  if (exists) {
                    setSelectedInviteUserIds(prev => prev.filter(id => id !== u.id));
                  } else {
                    setSelectedInviteUserIds(prev => [...prev, u.id]);
                  }
                }}
              >
                <Checkbox
                  edge="start"
                  checked={selectedInviteUserIds.includes(u.id)}
                  tabIndex={-1}
                  disableRipple
                />
                <ListItemText primary={u.displayName || u.username} secondary={u.email || ''} />
              </ListItem>
            ))}
            {inviteResults.length === 0 && (
              <ListItem>
                <ListItemText primary="No matching users" />
              </ListItem>
            )}
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setInviteDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleSendInvites}
            disabled={selectedInviteUserIds.length === 0}
          >
            Send Invites
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ChatComponent;
