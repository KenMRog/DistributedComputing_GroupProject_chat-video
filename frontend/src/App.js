import React, { useState } from 'react';
import CssBaseline from '@mui/material/CssBaseline';
import { Box, Typography, AppBar, Toolbar, IconButton } from '@mui/material';
import { Logout as LogoutIcon, Person as PersonIcon, LightMode as LightModeIcon, DarkMode as DarkModeIcon } from '@mui/icons-material';
import { motion, AnimatePresence } from 'framer-motion';
import ChatSidebar from './components/ChatSidebar';
import ChatMain from './components/ChatMain';
import AuthGuard from './components/AuthGuard';
import { SocketProvider } from './context/SocketContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider, useTheme } from './context/ThemeContext';
import logo from './static/logo.png';
import './App.css';

function AppContent() {
  const [selectedChat, setSelectedChat] = useState(null);
  const { user, logout } = useAuth();
  const { mode, toggleTheme } = useTheme();

  const handleLogout = () => {
    logout();
  };

  const handleChatSelect = (chat) => {
    setSelectedChat(chat);
  };

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
                  src={logo}
                  alt="Gatherly logo"
                  sx={{
                    height: 40,
                    width: 40,
                    objectFit: 'contain',
                    cursor: 'pointer',
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
              <motion.div
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.95 }}
              >
                <IconButton 
                  onClick={toggleTheme} 
                  color="primary" 
                  title={mode === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
                >
                  {mode === 'dark' ? <LightModeIcon /> : <DarkModeIcon />}
                </IconButton>
              </motion.div>
              <motion.div
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.95 }}
              >
                <IconButton 
                  onClick={handleLogout} 
                  color="primary" 
                  title="Logout"
                >
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
        />
        <AnimatePresence mode="wait">
          <ChatMain key={selectedChat?.id || 'empty'} selectedChat={selectedChat} user={user} />
        </AnimatePresence>
      </Box>
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
            <AppContent />
          </SocketProvider>
        </AuthGuard>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
