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
  Card,
  CardContent,
} from '@mui/material';
import { 
  Send as SendIcon, 
  Person as PersonIcon, 
  ScreenShare as ScreenShareIcon,
  Stop as StopIcon,
  FiberManualRecord as CircleIcon
} from '@mui/icons-material';
import { useSocket } from '../context/SocketContext';
import { useAuth } from '../context/AuthContext';

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
  
  // Get username from authenticated user
  const username = user?.username || user?.name || 'Anonymous';

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
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: '#1a1a1a' }}>
      {/* Chat Header */}
      <Paper sx={{ p: 2, mb: 2, borderRadius: 0, bgcolor: '#2d2d2d', color: 'white' }}>
        <Grid container spacing={2} alignItems="center">
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
                mr: 1 
              }} 
            />
          </Grid>
          <Grid item xs>
            <Typography variant="h6" sx={{ color: 'white' }}>
              {otherUser ? (otherUser.displayName || otherUser.username) : chatRoom?.name || 'Chat'}
            </Typography>
            <Typography variant="body2" sx={{ color: '#cccccc' }}>
              {otherUser ? (otherUser.username) : ''}
            </Typography>
          </Grid>
          <Grid item>
            <IconButton 
              color={isScreensharing ? "error" : "primary"}
              onClick={isScreensharing ? stopScreenShare : startScreenShare}
              title={isScreensharing ? "Stop Screen Share" : "Start Screen Share"}
              sx={{ color: 'white' }}
            >
              {isScreensharing ? <StopIcon /> : <ScreenShareIcon />}
            </IconButton>
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ flexGrow: 1, mb: 2, overflow: 'hidden', display: 'flex', flexDirection: 'column', bgcolor: '#1a1a1a' }}>
        <List sx={{ flexGrow: 1, overflow: 'auto', p: 1 }}>
          {messages.map((message, index) => {
            const isOwnMessage = message.senderId === user.id;
            return (
              <ListItem 
                key={index} 
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
                      sx={{ mb: 0.5, ml: 1, color: '#cccccc' }}
                    >
                      {message.sender}
                    </Typography>
                  )}
                  <Paper
                    elevation={1}
                    sx={{
                      p: 1.5,
                      bgcolor: isOwnMessage ? '#007AFF' : '#444444',
                      color: 'white',
                      borderRadius: isOwnMessage 
                        ? '18px 18px 4px 18px' 
                        : '18px 18px 18px 4px',
                      wordBreak: 'break-word'
                    }}
                  >
                    <Typography variant="body2" sx={{ color: 'white' }}>
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
                      color: '#cccccc'
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
            );
          })}
          <div ref={messagesEndRef} />
        </List>
      </Paper>

      <Paper component="form" onSubmit={handleSendMessage} sx={{ p: 2, bgcolor: '#2d2d2d' }}>
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
                bgcolor: '#444444',
                color: 'white',
                '& fieldset': {
                  border: '1px solid #666666'
                },
                '&:hover fieldset': {
                  border: '1px solid #888888'
                },
                '&.Mui-focused fieldset': {
                  border: '2px solid #007AFF'
                }
              },
              '& .MuiInputBase-input': {
                color: 'white',
                '&::placeholder': {
                  color: '#cccccc',
                  opacity: 1
                }
              }
            }}
          />
          <Button
            type="submit"
            variant="contained"
            sx={{
              minWidth: '50px',
              width: '50px',
              height: '50px',
              borderRadius: '50%',
              p: 0,
              bgcolor: newMessage.trim() ? '#007AFF' : '#666666',
              color: 'white',
              '&:hover': {
                bgcolor: newMessage.trim() ? '#0056CC' : '#666666'
              },
              '&:disabled': {
                bgcolor: '#666666',
                color: '#999999'
              }
            }}
            disabled={!newMessage.trim()}
          >
            <SendIcon />
          </Button>
        </Box>
      </Paper>

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
    </Box>
  );
};

export default ChatComponent;
