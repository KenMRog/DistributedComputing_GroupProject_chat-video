import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from '@mui/material';

export default function About({ open, onClose }) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>About Gatherly</DialogTitle>
      <DialogContent dividers>
        <Typography paragraph>
          Gatherly is a lightweight chat and screensharing web application. It pairs a React 
          frontend with a Spring Boot backend and supports real-time messaging over WebSockets (STOMP over SockJS).
        </Typography>

      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="primary">Close</Button>
      </DialogActions>
    </Dialog>
  );
}
