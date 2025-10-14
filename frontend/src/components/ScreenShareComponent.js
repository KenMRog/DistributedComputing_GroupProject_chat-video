import React, { useState, useRef, useEffect } from 'react';
import {
  Box,
  Paper,
  Button,
  Typography,
  Grid,
  Card,
  CardContent,
  Alert,
  Chip,
} from '@mui/material';
import {
  ScreenShare as ScreenShareIcon,
  Stop as StopIcon,
  Videocam as VideoIcon,
} from '@mui/icons-material';
import { useSocket } from '../context/SocketContext';

const ScreenShareComponent = () => {
  const [isSharing, setIsSharing] = useState(false);
  const [stream, setStream] = useState(null);
  const [error, setError] = useState('');
  const [remoteStreams, setRemoteStreams] = useState([]);
  const localVideoRef = useRef(null);
  const remoteVideoRefs = useRef({});
  const { connected, sendMessage, subscribe } = useSocket();

  useEffect(() => {
    if (connected && subscribe) {
      const subscription = subscribe('/topic/screenshare', (message) => {
        const data = JSON.parse(message.body);
        handleRemoteScreenData(data);
      });
      
      return () => {
        if (subscription) {
          subscription.unsubscribe();
        }
      };
    }
  }, [connected, subscribe]);

  const handleRemoteScreenData = (data) => {
    if (data.action === 'start' && data.userId !== getUserId()) {
      setRemoteStreams(prev => [...prev, { userId: data.userId, stream: data.data }]);
    } else if (data.action === 'stop' && data.userId !== getUserId()) {
      setRemoteStreams(prev => prev.filter(stream => stream.userId !== data.userId));
    }
  };

  const getUserId = () => {
    return localStorage.getItem('userId') || `user_${Date.now()}`;
  };

  const startScreenShare = async () => {
    try {
      setError('');
      const displayMediaStream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: true,
      });

      setStream(displayMediaStream);
      setIsSharing(true);

      if (localVideoRef.current) {
        localVideoRef.current.srcObject = displayMediaStream;
      }

      // Notify other users about screen share start
      sendMessage('/app/screenshare.start', {
        userId: getUserId(),
        roomId: 'default',
        action: 'start'
      });

      // Handle screen share end
      displayMediaStream.getVideoTracks()[0].onended = () => {
        stopScreenShare();
      };

    } catch (err) {
      console.error('Error starting screen share:', err);
      setError('Failed to start screen sharing. Please check permissions.');
    }
  };

  const stopScreenShare = () => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
      setStream(null);
      setIsSharing(false);

      // Notify other users about screen share stop
      sendMessage('/app/screenshare.stop', {
        userId: getUserId(),
        roomId: 'default',
        action: 'stop'
      });

      if (localVideoRef.current) {
        localVideoRef.current.srcObject = null;
      }
    }
  };

  if (!connected) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <Alert severity="warning">
          Connecting to screen share service...
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ height: '100%', p: 2 }}>
      <Grid container spacing={2} sx={{ height: '100%' }}>
        {/* Control Panel */}
        <Grid item xs={12}>
          <Paper sx={{ p: 2, mb: 2 }}>
            <Box display="flex" alignItems="center" gap={2} flexWrap="wrap">
              <Typography variant="h6">Screen Sharing</Typography>
              <Chip
                label={connected ? 'Connected' : 'Disconnected'}
                color={connected ? 'success' : 'error'}
                size="small"
              />
              
              {!isSharing ? (
                <Button
                  variant="contained"
                  startIcon={<ScreenShareIcon />}
                  onClick={startScreenShare}
                  color="primary"
                >
                  Start Screen Share
                </Button>
              ) : (
                <Button
                  variant="contained"
                  startIcon={<StopIcon />}
                  onClick={stopScreenShare}
                  color="secondary"
                >
                  Stop Screen Share
                </Button>
              )}
            </Box>
            
            {error && (
              <Alert severity="error" sx={{ mt: 2 }}>
                {error}
              </Alert>
            )}
          </Paper>
        </Grid>

        {/* Local Screen Share */}
        <Grid item xs={12} md={isSharing ? 6 : 12}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {isSharing ? 'Your Screen' : 'Screen Share Preview'}
              </Typography>
              
              {isSharing ? (
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
              ) : (
                <Box
                  sx={{
                    width: '100%',
                    height: 300,
                    bgcolor: 'grey.900',
                    borderRadius: 1,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexDirection: 'column',
                    gap: 2,
                  }}
                >
                  <VideoIcon sx={{ fontSize: 60, color: 'grey.600' }} />
                  <Typography variant="body1" color="text.secondary">
                    Click "Start Screen Share" to begin sharing your screen
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Remote Streams */}
        {remoteStreams.length > 0 && (
          <Grid item xs={12} md={6}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Remote Screens ({remoteStreams.length})
                </Typography>
                
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {remoteStreams.map((remoteStream, index) => (
                    <Box
                      key={index}
                      sx={{
                        width: '100%',
                        height: 150,
                        bgcolor: 'black',
                        borderRadius: 1,
                        overflow: 'hidden',
                        position: 'relative',
                      }}
                    >
                      <Typography
                        variant="caption"
                        sx={{
                          position: 'absolute',
                          top: 8,
                          left: 8,
                          bgcolor: 'rgba(0,0,0,0.7)',
                          color: 'white',
                          px: 1,
                          py: 0.5,
                          borderRadius: 0.5,
                          zIndex: 1,
                        }}
                      >
                        {remoteStream.userId}
                      </Typography>
                      <video
                        ref={el => remoteVideoRefs.current[remoteStream.userId] = el}
                        autoPlay
                        style={{
                          width: '100%',
                          height: '100%',
                          objectFit: 'contain',
                        }}
                      />
                    </Box>
                  ))}
                </Box>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>
    </Box>
  );
};

export default ScreenShareComponent;
