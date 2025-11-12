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
  GroupAdd as GroupAddIcon,
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
  const [newRoomDialogOpen, setNewRoomDialogOpen] = useState(false);
  const [inboxOpen, setInboxOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [inviteMessage, setInviteMessage] = useState('');
  const [allUsers, setAllUsers] = useState([]);
  const [inviteSearchTerm, setInviteSearchTerm] = useState('');
  const [inviteResults, setInviteResults] = useState([]);
  const { user } = useAuth();

  const [newRoomName, setNewRoomName] = useState('');
  const [newRoomDescription, setNewRoomDescription] = useState('');
  const [newRoomIsPrivate, setNewRoomIsPrivate] = useState(false);

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

  // Fetch users for new DM invite search (top 5 match)
  const fetchUsersForInvite = async (q) => {
    try {
      const base = `http://localhost:8080/api/users`;
      const url = q && q.trim() !== '' ? `${base}?q=${encodeURIComponent(q)}&excludeActiveDmWith=${user.id}` : `${base}?excludeActiveDmWith=${user.id}`;
      const response = await fetch(url);
      if (response.ok) {
        const data = await response.json();
        setInviteResults(data.filter(u => u.id !== user.id));
      }
    } catch (error) {
      console.error('Error fetching users for invite:', error);
    }
  };

  // Debounce search when new chat dialog open
  useEffect(() => {
    let t;
    if (newChatDialogOpen) {
      t = setTimeout(() => {
        fetchUsersForInvite(inviteSearchTerm);
      }, 300);
    }
    return () => clearTimeout(t);
  }, [inviteSearchTerm, newChatDialogOpen]);

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
          description: inviteMessage,
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
              <Badge badgeContent={pendingInvites.length} color="error">
                <MailIcon />
              </Badge>
            </IconButton>
            <IconButton onClick={() => setNewRoomDialogOpen(true)} color="primary" sx={{ mr: 1 }} title="Create Room">
              <GroupAddIcon />
            </IconButton>
            <IconButton onClick={() => setNewChatDialogOpen(true)} color="primary" title="New DM">
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

      {/* pending invites are shown via badge on the Inbox icon */}

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
                    primary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Box component="span" sx={{ fontWeight: 500 }}>{otherUser ? (otherUser.displayName || otherUser.username) : chat.name}</Box>
                        {(chat.roomType === 'PRIVATE' || chat.roomType === 'PUBLIC') && (
                          <Chip
                            size="small"
                            label={chat.roomType === 'PRIVATE' ? 'Private' : 'Public'}
                            variant="outlined"
                            color={chat.roomType === 'PRIVATE' ? 'default' : 'primary'}
                          />
                        )}
                      </Box>
                    }
                    secondary={
                      <Box>
                        <Typography variant="caption" color="text.secondary">
                          {formatLastActivity(chat.lastActivityAt)}
                        </Typography>
                        {chat.description && (
                          <Typography
                            variant="body2"
                            color="text.secondary"
                            noWrap
                            sx={{ mt: 0.5 }}
                          >
                            {chat.description.length > 90 ? chat.description.slice(0, 90) + '…' : chat.description}
                          </Typography>
                        )}
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
          <TextField
            fullWidth
            placeholder="Search users by name, username or email"
            value={inviteSearchTerm}
            onChange={(e) => setInviteSearchTerm(e.target.value)}
            sx={{ mb: 1 }}
          />

          <List sx={{ maxHeight: 200, overflow: 'auto' }}>
            {inviteResults.map((u) => (
              <ListItem key={u.id} button selected={selectedUserId === String(u.id)} onClick={() => setSelectedUserId(String(u.id))}>
                <ListItemAvatar>
                  <Avatar>{(u.displayName || u.username || '').charAt(0).toUpperCase()}</Avatar>
                </ListItemAvatar>
                <ListItemText primary={u.displayName || u.username} secondary={u.email || ''} />
              </ListItem>
            ))}
            {inviteResults.length === 0 && (
              <ListItem>
                <ListItemText primary="No matching users" />
              </ListItem>
            )}
          </List>

          <TextField
            fullWidth
            multiline
            rows={3}
            label="Description (required)"
            value={inviteMessage}
            onChange={(e) => setInviteMessage(e.target.value)}
            sx={{ mt: 2 }}
            placeholder="Describe the chat (this will be shown at the top and is searchable)"
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setNewChatDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleNewChat} variant="contained" disabled={!selectedUserId || !inviteMessage.trim()}>
            Send Invite
          </Button>
        </DialogActions>
      </Dialog>

      {/* New Room Dialog */}
      <Dialog open={newRoomDialogOpen} onClose={() => setNewRoomDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create New Room</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Room Name"
            value={newRoomName}
            onChange={(e) => setNewRoomName(e.target.value)}
            sx={{ mt: 1 }}
          />

          <TextField
            fullWidth
            multiline
            rows={3}
            label="Description (required)"
            value={newRoomDescription}
            onChange={(e) => setNewRoomDescription(e.target.value)}
            sx={{ mt: 2 }}
            placeholder="Describe the room (this will be shown at the top and is searchable)"
            required
          />

          <FormControl fullWidth sx={{ mt: 2 }}>
            <InputLabel id="room-privacy-label">Privacy</InputLabel>
            <Select
              labelId="room-privacy-label"
              value={newRoomIsPrivate ? 'private' : 'public'}
              label="Privacy"
              onChange={(e) => setNewRoomIsPrivate(e.target.value === 'private')}
            >
              <MenuItem value="public">Public (anyone can join)</MenuItem>
              <MenuItem value="private">Private (invite only)</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setNewRoomDialogOpen(false)}>Cancel</Button>
          <Button onClick={async () => {
            // Create room via backend
            try {
              const payload = {
                name: newRoomName,
                description: newRoomDescription,
                isPrivate: newRoomIsPrivate,
              };

              const response = await fetch(`http://localhost:8080/api/chat/rooms?creatorId=${user.id}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
              });

              if (response.ok) {
                setNewRoomDialogOpen(false);
                setNewRoomName('');
                setNewRoomDescription('');
                setNewRoomIsPrivate(false);
                // Refresh chat list
                fetchChatRooms();
              } else {
                console.error('Error creating room', response.statusText);
              }
            } catch (err) {
              console.error('Error creating room', err);
            }
          }} variant="contained" disabled={!newRoomName || !newRoomDescription.trim()}>
            Create
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
              {invite.chatRoom?.description && (
                <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
                  {invite.chatRoom.description}
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
