import React, { useState } from 'react';
import CssBaseline from '@mui/material/CssBaseline';
import { motion, AnimatePresence } from 'framer-motion';
import { Box, Typography, Button, AppBar, Toolbar, IconButton, Dialog, DialogTitle, DialogContent, DialogActions, DialogContentText } from '@mui/material';
import { Logout as LogoutIcon, Person as PersonIcon, Info as InfoIcon, LightMode as LightModeIcon, DarkMode as DarkModeIcon } from '@mui/icons-material';
import ChatSidebar from './components/ChatSidebar';
import ChatMain from './components/ChatMain';
import ScreenShare from './components/ScreenShare';
import AuthGuard from './components/AuthGuard';
import { SocketProvider } from './context/SocketContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider, useTheme } from './context/ThemeContext';
import { StreamProvider, useStream } from './context/StreamContext';
import logoNoText from './static/logonotext.png';
import './App.css';
import About from './components/About';

function AppContent() {
  const [selectedChat, setSelectedChat] = useState(null);
  const [aboutOpen, setAboutOpen] = useState(false);
  const { user, logout } = useAuth();
  const [logoutConfirmOpen, setLogoutConfirmOpen] = useState(false);
  const [leaveRoomConfirmOpen, setLeaveRoomConfirmOpen] = useState(false);
  const [pendingChat, setPendingChat] = useState(null);
  const [chatListRefreshKey, setChatListRefreshKey] = useState(0);
  const { isStreaming, stopStreaming } = useStream();

  const { mode, toggleTheme } = useTheme();

  const handleLogout = () => {
    
    // open confirmation dialog instead of logging out immediately
    setLogoutConfirmOpen(true);
  };

  const confirmLogout = () => {
    setLogoutConfirmOpen(false);
    setSelectedChat(null); // Clear selected chat before logout
    logout();
  };

  const cancelLogout = () => {
    setLogoutConfirmOpen(false);
  };

  const handleChatSelect = (chat) => {
    // If user is streaming and trying to switch to a different room, show confirmation
    if (isStreaming && selectedChat && chat.id !== selectedChat.id) {
      setPendingChat(chat);
      setLeaveRoomConfirmOpen(true);
      return;
    }
    
    // If user is streaming but trying to go back to the same room, allow it
    if (isStreaming && selectedChat && chat.id === selectedChat.id) {
      setSelectedChat(chat);
      return;
    }
    
    // Normal chat selection when not streaming
    setSelectedChat(chat);
  };
  
  const confirmLeaveRoom = () => {
    // Stop streaming before switching rooms
    if (isStreaming) {
      stopStreaming();
    }
    setLeaveRoomConfirmOpen(false);
    if (pendingChat) {
      setSelectedChat(pendingChat);
      setPendingChat(null);
    }
  };
  
  const cancelLeaveRoom = () => {
    setLeaveRoomConfirmOpen(false);
    setPendingChat(null);
  };

  const handleOpenAbout = () => setAboutOpen(true);
  const handleCloseAbout = () => setAboutOpen(false);

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.3 }}
      style={{ width: '100%', height: '100vh', display: 'flex', flexDirection: 'column' }}
    >
      <Box
        component={motion.div}
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.3, delay: 0.1 }}
      >
        <AppBar 
          position="static" 
          sx={{ bgcolor: 'background.paper', borderBottom: 1, borderColor: 'divider' }}
        >
          <Toolbar>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexGrow: 1 }}>
              <motion.div
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.3, delay: 0.2 }}
                whileHover={{ scale: 1.05 }}
              >
                <Box
                  component="img"
                  src={logoNoText}
                  alt="Gatherly logo"
                  sx={{
                    height: 40,
                    width: 40,
                    objectFit: 'contain',
                    cursor: 'pointer',
                    filter: mode === 'light' ? 'invert(1)' : 'none',
                    transition: 'filter 0.3s ease',
                  }}
                  onError={(e) => {
                    e.target.style.display = 'none';
                  }}
                />
              </motion.div>
              <motion.div
                initial={{ opacity: 0, x: -10 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3, delay: 0.2 }}
              >
                <Typography 
                  variant="h5" 
                  sx={{ color: 'primary.main', fontWeight: 600 }}
                >
                  Gatherly
                </Typography>
              </motion.div>
            </Box>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <PersonIcon color="primary" />
                <Typography variant="body2" color="text.secondary">
                  {user?.name || user?.email}
                </Typography>
              </Box>

              <motion.div whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.95 }}>
                <IconButton
                  onClick={toggleTheme}
                  color="primary"
                  title={mode === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
                >
                  {mode === 'dark' ? <LightModeIcon /> : <DarkModeIcon />}
                </IconButton>
              </motion.div>

              <motion.div whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.95 }}>
                <IconButton
                  onClick={handleOpenAbout}
                  color="primary"
                  title="About"
                  aria-label="About"
                  sx={{ color: 'primary.main' }}
                  size="large"
                >
                  <InfoIcon />
                </IconButton>
              </motion.div>

              <motion.div whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.95 }}>
                <IconButton onClick={handleLogout} color="primary" title="Logout">
                  <LogoutIcon />
                </IconButton>
              </motion.div>
            </Box>
          </Toolbar>
        </AppBar>
      </Box>

      <Box sx={{ flexGrow: 1, overflow: 'hidden', display: 'flex' }}>
        <ChatSidebar 
          selectedChatId={selectedChat?.id} 
          onChatSelect={handleChatSelect}
          refreshKey={chatListRefreshKey}
        />
        <AnimatePresence mode="wait">
          <ChatMain 
            key={selectedChat?.id || 'empty'} 
            selectedChat={selectedChat} 
            user={user}
            onLeaveRoom={() => {
              setSelectedChat(null);
              // Trigger refresh of chat list
              setChatListRefreshKey(prev => prev + 1);
            }}
          />
        </AnimatePresence>
      </Box>
      <About open={aboutOpen} onClose={handleCloseAbout} />

      <Dialog open={logoutConfirmOpen} onClose={cancelLogout}>
        <DialogTitle>Confirm Logout</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to log out?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={cancelLogout}>Cancel</Button>
          <Button onClick={confirmLogout} color="primary" variant="contained">Logout</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={leaveRoomConfirmOpen} onClose={cancelLeaveRoom}>
        <DialogTitle>Leave Chatroom While Streaming?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            You are currently sharing your screen in this chatroom. If you leave, your stream will end. 
            Are you sure you want to leave?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={cancelLeaveRoom}>Cancel</Button>
          <Button onClick={confirmLeaveRoom} color="primary" variant="contained">
            Leave and Stop Streaming
          </Button>
        </DialogActions>
      </Dialog>
    </motion.div>
  );
}

function App() {
  return (
    <ThemeProvider>
      <CssBaseline />
      <AuthProvider>
        <AuthGuard>
          <SocketProvider>
            <StreamProvider>
              <AppContent />
            </StreamProvider>
          </SocketProvider>
        </AuthGuard>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
