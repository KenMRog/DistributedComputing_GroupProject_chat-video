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
import { motion, AnimatePresence } from 'framer-motion';
import ChatComponent from './ChatComponent';

const ChatMain = ({ selectedChat, user, onLeaveRoom }) => {
  if (!selectedChat) {
    return (
      <motion.div
        key="empty"
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.95 }}
        transition={{ duration: 0.3 }}
        style={{
          flexGrow: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '2rem',
        }}
      >
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
          component={motion.div}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.1 }}
          sx={{
            p: 6,
            textAlign: 'center',
            bgcolor: 'background.paper',
            borderRadius: 2,
            boxShadow: 3,
          }}
        >
          <motion.div
            initial={{ scale: 0.8, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.4, delay: 0.2 }}
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
          </motion.div>
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.3 }}
          >
            <Typography variant="h4" gutterBottom color="text.primary">
              Hello, {user?.name || user?.username || 'User'}!
            </Typography>
          </motion.div>
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.4 }}
          >
            <Typography variant="h6" color="text.secondary">
              Open a chat to start the conversation.
            </Typography>
          </motion.div>
        </Paper>
        </Box>
      </motion.div>
    );
  }

  return (
    <motion.div
      key={selectedChat.id}
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -20 }}
      transition={{ duration: 0.3 }}
      style={{ flexGrow: 1, height: '100%' }}
    >
      <Box sx={{ flexGrow: 1, height: '100%' }}>
        <ChatComponent chatRoom={selectedChat} onLeaveRoom={onLeaveRoom} />
      </Box>
    </motion.div>
  );
};

export default ChatMain;
