import React, { useState, useEffect } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Box, Container, Typography, Button, AppBar, Toolbar, IconButton, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { Logout as LogoutIcon, Person as PersonIcon, Info as InfoIcon } from '@mui/icons-material';
import ChatSidebar from './components/ChatSidebar';
import ChatMain from './components/ChatMain';
import AuthGuard from './components/AuthGuard';
import { SocketProvider } from './context/SocketContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import './App.css';
import About from './components/About';

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
  const [aboutOpen, setAboutOpen] = useState(false);
  const { user, logout } = useAuth();
  const [logoutConfirmOpen, setLogoutConfirmOpen] = useState(false);

  const handleLogout = () => {
    // open confirmation dialog instead of logging out immediately
    setLogoutConfirmOpen(true);
  };

  const confirmLogout = () => {
    setLogoutConfirmOpen(false);
    logout();
  };

  const cancelLogout = () => {
    setLogoutConfirmOpen(false);
  };

  const handleChatSelect = (chat) => {
    setSelectedChat(chat);
  };

  const handleOpenAbout = () => setAboutOpen(true);
  const handleCloseAbout = () => setAboutOpen(false);

  return (
    <Box sx={{ width: '100%', height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static" sx={{ bgcolor: 'background.paper', borderBottom: 1, borderColor: 'divider' }}>
        <Toolbar>
          <Typography variant="h5" component="h1" sx={{ flexGrow: 1, color: 'primary.main' }}>
            Gatherly
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PersonIcon color="primary" />
              <Typography variant="body2" color="text.secondary">
                {user?.name || user?.email}
              </Typography>
            </Box>
            <IconButton onClick={handleOpenAbout} color="primary" title="About">
              <InfoIcon />
            </IconButton>
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
