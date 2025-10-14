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
import { Send as SendIcon } from '@mui/icons-material';
import { useSocket } from '../context/SocketContext';

const ChatComponent = () => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [username, setUsername] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const messagesEndRef = useRef(null);
  const { connected, sendMessage, subscribe } = useSocket();

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (connected && subscribe) {
      const subscription = subscribe('/topic/public', (message) => {
        const receivedMessage = JSON.parse(message.body);
        setMessages(prev => [...prev, receivedMessage]);
      });
      
      setIsConnected(true);
      
      return () => {
        if (subscription) {
          subscription.unsubscribe();
        }
      };
    }
  }, [connected, subscribe]);

  const handleSendMessage = (e) => {
    e.preventDefault();
    if (newMessage.trim() && username.trim()) {
      const message = {
        content: newMessage,
        sender: username,
        type: 'CHAT'
      };
      
      sendMessage('/app/chat.sendMessage', message);
      setNewMessage('');
    }
  };

  const handleJoinChat = () => {
    if (username.trim()) {
      const joinMessage = {
        content: '',
        sender: username,
        type: 'JOIN'
      };
      
      sendMessage('/app/chat.addUser', joinMessage);
    }
  };

  if (!isConnected) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <Typography variant="h6">Connecting to chat...</Typography>
      </Box>
    );
  }

  if (!username) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <Paper sx={{ p: 4, maxWidth: 400, width: '100%' }}>
          <Typography variant="h5" gutterBottom align="center">
            Join Chat
          </Typography>
          <TextField
            fullWidth
            label="Enter your username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleJoinChat()}
            sx={{ mb: 2 }}
          />
          <Button
            fullWidth
            variant="contained"
            onClick={handleJoinChat}
            disabled={!username.trim()}
          >
            Join Chat
          </Button>
        </Paper>
      </Box>
    );
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item>
            <Chip
              label={isConnected ? 'Connected' : 'Disconnected'}
              color={isConnected ? 'success' : 'error'}
              size="small"
            />
          </Grid>
          <Grid item>
            <Typography variant="subtitle1">
              Welcome, {username}!
            </Typography>
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
