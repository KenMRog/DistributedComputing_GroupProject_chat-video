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
} from '@mui/material';
import { Send as SendIcon, Person as PersonIcon } from '@mui/icons-material';
import { useSocket } from '../context/SocketContext';
import { useAuth } from '../context/AuthContext';

const ChatComponent = ({ chatRoom }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const messagesEndRef = useRef(null);
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

  useEffect(() => {
    if (connected && subscribe && username && chatRoom) {
      const topic = `/topic/chat/${chatRoom.id}`;
      const subscription = subscribe(topic, (message) => {
        const receivedMessage = JSON.parse(message.body);
        setMessages(prev => [...prev, receivedMessage]);
      });
      
      setIsConnected(true);
      
      // Automatically join chat when connected
      const joinMessage = {
        content: '',
        sender: username,
        type: 'JOIN'
      };
      sendMessage(`/app/chat/${chatRoom.id}/addUser`, joinMessage);
      
      return () => {
        if (subscription) {
          subscription.unsubscribe();
        }
      };
    }
  }, [connected, subscribe, username, sendMessage, chatRoom]);

  const handleSendMessage = (e) => {
    e.preventDefault();
    if (newMessage.trim() && username && chatRoom) {
      const message = {
        content: newMessage,
        sender: username,
        type: 'CHAT'
      };
      
      sendMessage(`/app/chat/${chatRoom.id}/sendMessage`, message);
      setNewMessage('');
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
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Chat Header */}
      <Paper sx={{ p: 2, mb: 2, borderRadius: 0 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item>
            <Avatar sx={{ bgcolor: 'primary.main' }}>
              {otherUser ? (otherUser.displayName || otherUser.username).charAt(0).toUpperCase() : <PersonIcon />}
            </Avatar>
          </Grid>
          <Grid item xs>
            <Typography variant="h6">
              {otherUser ? (otherUser.displayName || otherUser.username) : chatRoom?.name || 'Chat'}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {otherUser ? (otherUser.username) : ''}
            </Typography>
          </Grid>
          <Grid item>
            <Chip
              label={isConnected ? 'Connected' : 'Disconnected'}
              color={isConnected ? 'success' : 'error'}
              size="small"
            />
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ flexGrow: 1, mb: 2, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
        <List sx={{ flexGrow: 1, overflow: 'auto', p: 1 }}>
          {messages.map((message, index) => (
            <ListItem key={index} alignItems="flex-start" sx={{ mb: 1 }}>
              <Avatar sx={{ mr: 2, bgcolor: 'primary.main' }}>
                {message.sender?.charAt(0).toUpperCase()}
              </Avatar>
              <ListItemText
                primary={
                  <Box display="flex" alignItems="center" gap={1}>
                    <Typography variant="subtitle2" color="primary">
                      {message.sender}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {message.timestamp && new Date(message.timestamp).toLocaleTimeString()}
                    </Typography>
                  </Box>
                }
                secondary={
                  <Typography variant="body2">
                    {message.content}
                  </Typography>
                }
              />
            </ListItem>
          ))}
          <div ref={messagesEndRef} />
        </List>
      </Paper>

      <Paper component="form" onSubmit={handleSendMessage} sx={{ p: 2 }}>
        <Box display="flex" gap={1}>
          <TextField
            fullWidth
            variant="outlined"
            placeholder="Type your message..."
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSendMessage(e)}
          />
          <Button
            type="submit"
            variant="contained"
            endIcon={<SendIcon />}
            disabled={!newMessage.trim()}
          >
            Send
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default ChatComponent;
