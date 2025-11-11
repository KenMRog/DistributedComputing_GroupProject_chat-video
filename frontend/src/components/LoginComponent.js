import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  Tabs,
  Tab,
  Container
} from '@mui/material';
import logo from '../static/logo.png';
import { useAuth } from '../context/AuthContext';

function TabPanel({ children, value, index, ...other }) {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`auth-tabpanel-${index}`}
      aria-labelledby={`auth-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ pt: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

const LoginComponent = () => {
  const [currentTab, setCurrentTab] = useState(0);
  const [loginData, setLoginData] = useState({ email: '', password: '' });
  const [registerData, setRegisterData] = useState({ 
    username: '',
    email: '', 
    password: '', 
    confirmPassword: '' 
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();

  const handleTabChange = (event, newValue) => {
    setCurrentTab(newValue);
    setError('');
  };

  const handleLoginChange = (field) => (event) => {
    setLoginData(prev => ({
      ...prev,
      [field]: event.target.value
    }));
    setError('');
  };

  const handleRegisterChange = (field) => (event) => {
    setRegisterData(prev => ({
      ...prev,
      [field]: event.target.value
    }));
    setError('');
  };

  const handleLogin = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');

    const result = await login(loginData.email, loginData.password);
    
    if (!result.success) {
      setError(result.error);
    }
    
    setLoading(false);
  };

  const handleRegister = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');

    const result = await register(
      registerData.email, 
      registerData.password, 
      registerData.confirmPassword,
      registerData.username
    );
    
    if (!result.success) {
      setError(result.error);
    }
    
    setLoading(false);
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 6, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      {/* Large logo above the login card */}
        <Box className="logo-bg logo-bg--solid" sx={{ width: 200, height: 200, mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Box component="img" src={logo} alt="Gatherly logo" sx={{ width: 160, height: 160, objectFit: 'contain', display: 'block' }} onError={(e)=>{e.target.style.display='none'}} />
        </Box>

      <Card sx={{ maxWidth: 500, mx: 'auto' }}>
        <CardContent>
          <Typography variant="h4" component="h1" align="center" gutterBottom>
            Welcome to Gatherly
          </Typography>
          <Typography variant="body2" align="center" color="text.secondary" sx={{ mb: 3 }}>
            Sign in or create an account to continue
          </Typography>
          
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={currentTab} onChange={handleTabChange} centered>
              <Tab label="Sign In" />
              <Tab label="Sign Up" />
            </Tabs>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {error}
            </Alert>
          )}

          <TabPanel value={currentTab} index={0}>
            <Box component="form" onSubmit={handleLogin}>
              <TextField
                fullWidth
                label="Email"
                type="email"
                value={loginData.email}
                onChange={handleLoginChange('email')}
                margin="normal"
                required
                autoComplete="email"
              />
              <TextField
                fullWidth
                label="Password"
                type="password"
                value={loginData.password}
                onChange={handleLoginChange('password')}
                margin="normal"
                required
                autoComplete="current-password"
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={loading}
              >
                {loading ? <CircularProgress size={24} /> : 'Sign In'}
              </Button>
            </Box>
          </TabPanel>

          <TabPanel value={currentTab} index={1}>
            <Box component="form" onSubmit={handleRegister}>
              <TextField
                fullWidth
                label="Username"
                type="text"
                value={registerData.username}
                onChange={handleRegisterChange('username')}
                margin="normal"
                required
                autoComplete="username"
              />
              <TextField
                fullWidth
                label="Email"
                type="email"
                value={registerData.email}
                onChange={handleRegisterChange('email')}
                margin="normal"
                required
                autoComplete="email"
              />
              <TextField
                fullWidth
                label="Password"
                type="password"
                value={registerData.password}
                onChange={handleRegisterChange('password')}
                margin="normal"
                required
                autoComplete="new-password"
              />
              <TextField
                fullWidth
                label="Confirm Password"
                type="password"
                value={registerData.confirmPassword}
                onChange={handleRegisterChange('confirmPassword')}
                margin="normal"
                required
                autoComplete="new-password"
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={loading}
              >
                {loading ? <CircularProgress size={24} /> : 'Sign Up'}
              </Button>
            </Box>
          </TabPanel>
        </CardContent>
      </Card>
    </Container>
  );
};

export default LoginComponent;
