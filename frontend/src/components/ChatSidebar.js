import React, { useState, useEffect } from 'react';
import {
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Typography,
  IconButton,
  TextField,
  InputAdornment,
  Chip,
  Badge,
  Divider,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import {
  Search as SearchIcon,
  Add as AddIcon,
  Person as PersonIcon,
  Chat as ChatIcon,
  Notifications as NotificationsIcon,
  Mail as MailIcon,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';

const ChatSidebar = ({ selectedChatId, onChatSelect, onNewChat }) => {
  const [chats, setChats] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [pendingInvites, setPendingInvites] = useState([]);
  const [newChatDialogOpen, setNewChatDialogOpen] = useState(false);
  const [inboxOpen, setInboxOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [inviteMessage, setInviteMessage] = useState('');
  const [allUsers, setAllUsers] = useState([]);
  const { user } = useAuth();

  // Fetch user's chat rooms
  const fetchChatRooms = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/chat/rooms?userId=${user.id}`);
      if (response.ok) {
        const data = await response.json();
        setChats(data);
      }
    } catch (error) {
      console.error('Error fetching chat rooms:', error);
    }
  };

  // Fetch pending invites
  const fetchPendingInvites = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/chat/invites/pending?userId=${user.id}`);
      if (response.ok) {
        const data = await response.json();
        setPendingInvites(data);
      }
    } catch (error) {
      console.error('Error fetching pending invites:', error);
    }
  };

  // Fetch all users for new chat
  const fetchAllUsers = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/users');
      if (response.ok) {
        const data = await response.json();
        setAllUsers(data);
      } else {
        console.error('Error fetching users:', response.statusText);
      }
    } catch (error) {
      console.error('Error fetching users:', error);
    }
  };

  useEffect(() => {
    if (user) {
      fetchChatRooms();
      fetchPendingInvites();
      fetchAllUsers();
    }
  }, [user]);

  const handleNewChat = async () => {
    if (!selectedUserId) return;

    try {
      const response = await fetch(`http://localhost:8080/api/chat/invite?inviterId=${user.id}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          invitedUserId: parseInt(selectedUserId),
          message: inviteMessage,
        }),
      });

      if (response.ok) {
        setNewChatDialogOpen(false);
        setSelectedUserId('');
        setInviteMessage('');
        // Refresh the chat list
        fetchChatRooms();
      }
    } catch (error) {
      console.error('Error creating chat invite:', error);
    }
  };

  const handleAcceptInvite = async (inviteId) => {
    try {
      const response = await fetch(`http://localhost:8080/api/chat/invite/${inviteId}/accept?userId=${user.id}`, {
        method: 'POST',
      });

      if (response.ok) {
        // Refresh the chat list and invites
        fetchChatRooms();
        fetchPendingInvites();
      }
    } catch (error) {
      console.error('Error accepting invite:', error);
    }
  };

  const handleDeclineInvite = async (inviteId) => {
    try {
      const response = await fetch(`http://localhost:8080/api/chat/invite/${inviteId}/decline?userId=${user.id}`, {
        method: 'POST',
      });

      if (response.ok) {
        // Refresh the invites
        fetchPendingInvites();
      }
    } catch (error) {
      console.error('Error declining invite:', error);
    }
  };

  const filteredChats = chats.filter(chat =>
    chat.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getOtherUser = (chat) => {
    if (chat.members && chat.members.length === 2) {
      return chat.members.find(member => member.id !== user.id);
    }
    return null;
  };

  const formatLastActivity = (lastActivity) => {
    if (!lastActivity) return '';
    const date = new Date(lastActivity);
    const now = new Date();
    const diffInHours = (now - date) / (1000 * 60 * 60);
    
    if (diffInHours < 24) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else if (diffInHours < 168) { // 7 days
      return date.toLocaleDateString([], { weekday: 'short' });
    } else {
      return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
    }
  };

  return (
    <Box sx={{ width: 300, height: '100%', display: 'flex', flexDirection: 'column', bgcolor: 'background.paper' }}>
      {/* Header */}
      <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Typography variant="h6" component="h1">
            Chats
          </Typography>
          <Box>
            <IconButton onClick={() => setInboxOpen(true)} color="primary" sx={{ mr: 1 }} title="Inbox">
              <MailIcon />
            </IconButton>
            <IconButton onClick={() => setNewChatDialogOpen(true)} color="primary">
              <AddIcon />
            </IconButton>
          </Box>
        </Box>
        
        {/* Search */}
        <TextField
          fullWidth
          size="small"
          placeholder="Find chats"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
        />
      </Box>

      {/* Pending Invites */}
      {pendingInvites.length > 0 && (
        <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
          <Typography variant="subtitle2" color="primary" sx={{ mb: 1 }}>
            Message Requests
          </Typography>
          {pendingInvites.map((invite) => (
            <Box key={invite.id} sx={{ mb: 1, p: 1, bgcolor: 'action.hover', borderRadius: 1 }}>
              <Typography variant="body2" sx={{ mb: 1 }}>
                {invite.inviter.displayName || invite.inviter.username} wants to chat
              </Typography>
              {invite.message && (
                <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
                  "{invite.message}"
                </Typography>
              )}
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button size="small" variant="contained" onClick={() => handleAcceptInvite(invite.id)}>
                  Accept
                </Button>
                <Button size="small" variant="outlined" onClick={() => handleDeclineInvite(invite.id)}>
                  Decline
                </Button>
              </Box>
            </Box>
          ))}
        </Box>
      )}

      {/* Chat List */}
      <Box sx={{ flexGrow: 1, overflow: 'auto' }}>
        <List>
          {filteredChats.map((chat) => {
            const otherUser = getOtherUser(chat);
            const isSelected = selectedChatId === chat.id;
            
            return (
              <ListItem key={chat.id} disablePadding>
                <ListItemButton
                  selected={isSelected}
                  onClick={() => onChatSelect(chat)}
                  sx={{
                    '&.Mui-selected': {
                      bgcolor: 'primary.main',
                      color: 'primary.contrastText',
                      '&:hover': {
                        bgcolor: 'primary.dark',
                      },
                    },
                  }}
                >
                  <ListItemAvatar>
                    <Avatar sx={{ bgcolor: 'primary.main' }}>
                      {otherUser ? (otherUser.displayName || otherUser.username).charAt(0).toUpperCase() : <PersonIcon />}
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={otherUser ? (otherUser.displayName || otherUser.username) : chat.name}
                    secondary={
                      <Box>
                        <Typography variant="caption" color="text.secondary">
                          {formatLastActivity(chat.lastActivityAt)}
                        </Typography>
                      </Box>
                    }
                  />
                </ListItemButton>
              </ListItem>
            );
          })}
        </List>
      </Box>

      {/* New Chat Dialog */}
      <Dialog open={newChatDialogOpen} onClose={() => setNewChatDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Start New Chat</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mt: 2 }}>
            <InputLabel>Select User</InputLabel>
            <Select
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              label="Select User"
            >
              {allUsers
                .filter(u => u.id !== user.id)
                .map((user) => (
                  <MenuItem key={user.id} value={user.id}>
                    {user.displayName || user.username}
                  </MenuItem>
                ))}
            </Select>
          </FormControl>
          
          <TextField
            fullWidth
            multiline
            rows={3}
            label="Message (optional)"
            value={inviteMessage}
            onChange={(e) => setInviteMessage(e.target.value)}
            sx={{ mt: 2 }}
            placeholder="Send a message with your invite..."
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setNewChatDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleNewChat} variant="contained" disabled={!selectedUserId}>
            Send Invite
          </Button>
        </DialogActions>
      </Dialog>

      {/* Inbox Dialog */}
      <Dialog open={inboxOpen} onClose={() => setInboxOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Inbox</DialogTitle>
        <DialogContent>
          {pendingInvites.length === 0 && (
            <Typography variant="body2">No pending invites</Typography>
          )}

          {pendingInvites.map((invite) => (
            <Box key={invite.id} sx={{ mb: 1, p: 1, bgcolor: 'action.hover', borderRadius: 1 }}>
              <Typography variant="body2" sx={{ mb: 1 }}>
                {invite.inviter.displayName || invite.inviter.username} wants to chat
              </Typography>
              {invite.message && (
                <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
                  "{invite.message}"
                </Typography>
              )}
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button size="small" variant="contained" onClick={() => { handleAcceptInvite(invite.id); setInboxOpen(false); }}>
                  Accept
                </Button>
                <Button size="small" variant="outlined" onClick={() => { handleDeclineInvite(invite.id); }}>
                  Decline
                </Button>
              </Box>
            </Box>
          ))}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setInboxOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ChatSidebar;
