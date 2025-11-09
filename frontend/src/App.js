import React, { useState, useEffect } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Box, Container, Typography, Button, AppBar, Toolbar, IconButton } from '@mui/material';
import { Logout as LogoutIcon, Person as PersonIcon } from '@mui/icons-material';
import ChatSidebar from './components/ChatSidebar';
import ChatMain from './components/ChatMain';
import AuthGuard from './components/AuthGuard';
import { SocketProvider } from './context/SocketContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import './App.css';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#5865f2',
    },
    secondary: {
      main: '#f04747',
    },
    background: {
      default: '#36393f',
      paper: '#2f3136',
    },
  },
});

function AppContent() {
  const [selectedChat, setSelectedChat] = useState(null);
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  const handleChatSelect = (chat) => {
    setSelectedChat(chat);
  };

  return (
    <Box sx={{ width: '100%', height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static" sx={{ bgcolor: 'background.paper', borderBottom: 1, borderColor: 'divider' }}>
        <Toolbar>
          <Typography variant="h5" component="h1" sx={{ flexGrow: 1, color: 'primary.main' }}>
            ChatApp
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PersonIcon color="primary" />
              <Typography variant="body2" color="text.secondary">
                {user?.name || user?.email}
              </Typography>
            </Box>
            <IconButton onClick={handleLogout} color="primary" title="Logout">
              <LogoutIcon />
            </IconButton>
          </Box>
        </Toolbar>
      </AppBar>
      
      <Box sx={{ flexGrow: 1, overflow: 'hidden', display: 'flex' }}>
        <ChatSidebar 
          selectedChatId={selectedChat?.id} 
          onChatSelect={handleChatSelect}
        />
        <ChatMain selectedChat={selectedChat} user={user} />
      </Box>
    </Box>
  );
}

function App() {
  return (
    <ThemeProvider theme={theme}>
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
