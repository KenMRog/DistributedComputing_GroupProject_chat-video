import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Paper,
  Typography,
  IconButton,
  Avatar,
  Chip,
  Tooltip,
} from '@mui/material';
import {
  Chat as ChatIcon,
  Person as PersonIcon,
} from '@mui/icons-material';

const ScreenShareView = ({ 
  activeShares, 
  localStream, 
  onBackToChat,
  currentUser,
  chatRoom 
}) => {
  const [selectedShareId, setSelectedShareId] = useState(null);
  const mainVideoRef = useRef(null);
  const thumbnailRefs = useRef({});

  // Set first share as selected by default
  useEffect(() => {
    if (activeShares.length > 0 && !selectedShareId) {
      // Prefer selecting the current user's share if they're sharing
      const ownShare = activeShares.find(s => s.userId === currentUser?.id);
      setSelectedShareId(ownShare ? ownShare.userId : activeShares[0].userId);
    }
  }, [activeShares, selectedShareId, currentUser?.id]);

  // Update main video when selection changes
  useEffect(() => {
    if (mainVideoRef.current && selectedShareId) {
      const selectedShare = activeShares.find(s => s.userId === selectedShareId);
      let streamToUse = null;
      
      // If it's the current user's share, use localStream if available
      if (selectedShareId === currentUser?.id && localStream) {
        streamToUse = localStream;
      } else if (selectedShare && selectedShare.stream) {
        streamToUse = selectedShare.stream;
      }
      
      if (streamToUse) {
        mainVideoRef.current.srcObject = streamToUse;
        // Ensure video plays
        mainVideoRef.current.play().catch(err => {
          console.error('Error playing video:', err);
        });
      } else if (mainVideoRef.current) {
        // Clear video if stream is not available
        mainVideoRef.current.srcObject = null;
      }
    }
  }, [selectedShareId, activeShares, localStream, currentUser?.id]);

  // Update thumbnail videos
  useEffect(() => {
    activeShares.forEach(share => {
      const ref = thumbnailRefs.current[share.userId];
      if (ref && share.stream) {
        ref.srcObject = share.stream;
      }
    });
  }, [activeShares]);

  const selectedShare = activeShares.find(s => s.userId === selectedShareId);
  const thumbnailShares = activeShares.filter(s => s.userId !== selectedShareId);

  // Helper to safely get the first character of a value (handles numbers/objects)
  const firstChar = (val) => {
    if (val === undefined || val === null) return '';
    const s = typeof val === 'string' ? val : String(val);
    return s.charAt(0).toUpperCase();
  };

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: '#1a1a1a' }}>
      {/* Header */}
      <Paper sx={{ p: 2, borderRadius: 0, bgcolor: '#2d2d2d', color: 'white' }}>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box display="flex" alignItems="center" gap={2}>
            <Typography variant="h6">
              {chatRoom?.name || 'Screen Sharing'}
            </Typography>
            <Chip 
              label={`${activeShares.length} ${activeShares.length === 1 ? 'screen' : 'screens'}`}
              color="primary"
              size="small"
            />
          </Box>
          <Tooltip title="Back to Chat">
            <IconButton 
              onClick={onBackToChat}
              sx={{ color: 'white' }}
            >
              <ChatIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Paper>

      {/* Main Content Area */}
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', p: 2, gap: 2 }}>
        {/* Main Screen Display */}
        <Box 
          sx={{ 
            flexGrow: 1, 
            bgcolor: '#000', 
            borderRadius: 2, 
            overflow: 'hidden',
            position: 'relative',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: 0,
          }}
        >
          {selectedShare ? (
            <>
              {(selectedShare.stream || (selectedShare.userId === currentUser?.id && localStream)) ? (
                <video
                  ref={(el) => {
                    mainVideoRef.current = el;
                    if (el && selectedShareId) {
                      const share = activeShares.find(s => s.userId === selectedShareId);
                      let streamToUse = null;
                      if (selectedShareId === currentUser?.id && localStream) {
                        streamToUse = localStream;
                      } else if (share && share.stream) {
                        streamToUse = share.stream;
                      }
                      if (streamToUse) {
                        el.srcObject = streamToUse;
                        el.play().catch(err => console.error('Error playing video:', err));
                      }
                    }
                  }}
                  autoPlay
                  playsInline
                  muted={selectedShare.userId === currentUser?.id}
                  style={{
                    width: '100%',
                    height: '100%',
                    objectFit: 'contain',
                    backgroundColor: '#000',
                  }}
                />
              ) : (
                <Box sx={{ textAlign: 'center', color: 'white', width: '100%', height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                  <PersonIcon sx={{ fontSize: 60, mb: 2, opacity: 0.5 }} />
                  <Typography variant="body1" sx={{ opacity: 0.7 }}>
                    Connecting to {selectedShare.username || selectedShare.displayName || `User ${selectedShare.userId}`}...
                  </Typography>
                </Box>
              )}
              {/* User label overlay */}
              <Box
                sx={{
                  position: 'absolute',
                  bottom: 16,
                  left: 16,
                  bgcolor: 'rgba(0, 0, 0, 0.7)',
                  color: 'white',
                  px: 2,
                  py: 1,
                  borderRadius: 1,
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1,
                }}
              >
                <Avatar sx={{ width: 24, height: 24, bgcolor: 'primary.main' }}>
                  {firstChar(selectedShare.username || selectedShare.userId)}
                </Avatar>
                <Typography variant="body2">
                  {selectedShare.username || selectedShare.displayName || `User ${selectedShare.userId}`}
                </Typography>
                {selectedShare.userId === currentUser?.id && (
                  <Chip label="You" size="small" color="primary" sx={{ ml: 1 }} />
                )}
                {!selectedShare.stream && !(selectedShare.userId === currentUser?.id && localStream) && (
                  <Chip label="Connecting..." size="small" color="warning" sx={{ ml: 1 }} />
                )}
              </Box>
            </>
          ) : (
            <Box sx={{ textAlign: 'center', color: 'white' }}>
              <PersonIcon sx={{ fontSize: 60, mb: 2, opacity: 0.5 }} />
              <Typography variant="h6" sx={{ opacity: 0.7 }}>
                No screen selected
              </Typography>
            </Box>
          )}
        </Box>

        {/* Thumbnail Row */}
        {thumbnailShares.length > 0 && (
          <Box
            sx={{
              display: 'flex',
              gap: 2,
              overflowX: 'auto',
              pb: 1,
              '&::-webkit-scrollbar': {
                height: 8,
              },
              '&::-webkit-scrollbar-track': {
                bgcolor: '#2d2d2d',
                borderRadius: 1,
              },
              '&::-webkit-scrollbar-thumb': {
                bgcolor: '#555',
                borderRadius: 1,
                '&:hover': {
                  bgcolor: '#666',
                },
              },
            }}
          >
            {thumbnailShares.map((share) => (
              <Box
                key={share.userId}
                onClick={() => setSelectedShareId(share.userId)}
                sx={{
                  minWidth: 200,
                  height: 120,
                  bgcolor: '#000',
                  borderRadius: 1,
                  overflow: 'hidden',
                  cursor: 'pointer',
                  position: 'relative',
                  border: '2px solid transparent',
                  '&:hover': {
                    borderColor: 'primary.main',
                    transform: 'scale(1.05)',
                  },
                  transition: 'all 0.2s',
                }}
              >
                <video
                  ref={(el) => {
                    thumbnailRefs.current[share.userId] = el;
                  }}
                  autoPlay
                  playsInline
                  muted
                  style={{
                    width: '100%',
                    height: '100%',
                    objectFit: 'cover',
                  }}
                />
                {/* Thumbnail label */}
                <Box
                  sx={{
                    position: 'absolute',
                    bottom: 0,
                    left: 0,
                    right: 0,
                    bgcolor: 'rgba(0, 0, 0, 0.7)',
                    color: 'white',
                    px: 1,
                    py: 0.5,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 0.5,
                  }}
                >
                  <Avatar sx={{ width: 16, height: 16, bgcolor: 'primary.main', fontSize: 10 }}>
                    {firstChar(share.username || share.userId)}
                  </Avatar>
                  <Typography variant="caption" sx={{ fontSize: '0.7rem' }}>
                    {share.username || share.displayName || `User ${share.userId}`}
                  </Typography>
                </Box>
              </Box>
            ))}
          </Box>
        )}

        {/* Show local stream in thumbnails if sharing */}
        {localStream && (
          <Box
            onClick={() => {
              // Find local share and select it
              const localShare = activeShares.find(s => s.userId === currentUser?.id);
              if (localShare) {
                setSelectedShareId(localShare.userId);
              }
            }}
            sx={{
              minWidth: 200,
              height: 120,
              bgcolor: '#000',
              borderRadius: 1,
              overflow: 'hidden',
              cursor: 'pointer',
              position: 'relative',
              border: selectedShareId === currentUser?.id ? '2px solid primary.main' : '2px solid transparent',
              '&:hover': {
                borderColor: 'primary.main',
                transform: 'scale(1.05)',
              },
              transition: 'all 0.2s',
            }}
          >
            <video
              ref={(el) => {
                if (el) el.srcObject = localStream;
              }}
              autoPlay
              playsInline
              muted
              style={{
                width: '100%',
                height: '100%',
                objectFit: 'cover',
              }}
            />
            <Box
              sx={{
                position: 'absolute',
                bottom: 0,
                left: 0,
                right: 0,
                bgcolor: 'rgba(0, 0, 0, 0.7)',
                color: 'white',
                px: 1,
                py: 0.5,
                display: 'flex',
                alignItems: 'center',
                gap: 0.5,
              }}
            >
              <Avatar sx={{ width: 16, height: 16, bgcolor: 'primary.main', fontSize: 10 }}>
                {firstChar(currentUser?.username || currentUser?.name || 'You')}
              </Avatar>
              <Typography variant="caption" sx={{ fontSize: '0.7rem' }}>
                You (Your Screen)
              </Typography>
            </Box>
          </Box>
        )}
      </Box>
    </Box>
  );
};

export default ScreenShareView;

