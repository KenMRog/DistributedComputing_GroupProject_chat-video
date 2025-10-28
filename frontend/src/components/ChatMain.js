import React from 'react';
import {
  Box,
  Typography,
  Avatar,
  Paper,
} from '@mui/material';
import {
  Chat as ChatIcon,
} from '@mui/icons-material';
import ChatComponent from './ChatComponent';

const ChatMain = ({ selectedChat, user }) => {
  if (!selectedChat) {
    return (
      <Box
        sx={{
          flexGrow: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: 'background.default',
          p: 4,
        }}
      >
        <Paper
          sx={{
            p: 6,
            textAlign: 'center',
            bgcolor: 'background.paper',
            borderRadius: 2,
            boxShadow: 3,
          }}
        >
          <Avatar
            sx={{
              width: 80,
              height: 80,
              bgcolor: 'primary.main',
              mx: 'auto',
              mb: 3,
            }}
          >
            <ChatIcon sx={{ fontSize: 40 }} />
          </Avatar>
          <Typography variant="h4" gutterBottom color="text.primary">
            Hello, {user?.name || user?.username || 'User'}!
          </Typography>
          <Typography variant="h6" color="text.secondary">
            Open a chat to start the conversation.
          </Typography>
        </Paper>
      </Box>
    );
  }

  return (
    <Box sx={{ flexGrow: 1, height: '100%' }}>
      <ChatComponent chatRoom={selectedChat} />
    </Box>
  );
};

export default ChatMain;
