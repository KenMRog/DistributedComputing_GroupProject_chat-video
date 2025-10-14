import React, { useState, useEffect } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Box, Container, Typography, Tabs, Tab } from '@mui/material';
import ChatComponent from './components/ChatComponent';
import ScreenShareComponent from './components/ScreenShareComponent';
import { SocketProvider } from './context/SocketContext';
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

function TabPanel({ children, value, index, ...other }) {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

function App() {
  const [currentTab, setCurrentTab] = useState(0);

  const handleTabChange = (event, newValue) => {
    setCurrentTab(newValue);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <SocketProvider>
        <Box sx={{ width: '100%', height: '100vh', display: 'flex', flexDirection: 'column' }}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: 'background.paper' }}>
            <Container maxWidth="xl">
              <Typography variant="h4" component="h1" sx={{ py: 2, color: 'primary.main' }}>
                ScreenShare App
              </Typography>
              <Tabs value={currentTab} onChange={handleTabChange} aria-label="app tabs">
                <Tab label="Chat" />
                <Tab label="Screen Share" />
              </Tabs>
            </Container>
          </Box>
          
          <Box sx={{ flexGrow: 1, overflow: 'hidden' }}>
            <TabPanel value={currentTab} index={0}>
              <ChatComponent />
            </TabPanel>
            <TabPanel value={currentTab} index={1}>
              <ScreenShareComponent />
            </TabPanel>
          </Box>
        </Box>
      </SocketProvider>
    </ThemeProvider>
  );
}

export default App;
