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
import { motion, AnimatePresence } from 'framer-motion';
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
      <AnimatePresence mode="wait">
        {value === index && (
          <motion.div
            key={index}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
          >
            <Box sx={{ pt: 3 }}>
              {children}
            </Box>
          </motion.div>
        )}
      </AnimatePresence>
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
    <Container 
      maxWidth="sm" 
      component={motion.div}
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.4 }}
      sx={{ mt: 6, display: 'flex', flexDirection: 'column', alignItems: 'center' }}
    >
      {/* Large logo above the login card */}
      <motion.div
        initial={{ scale: 0.8, opacity: 0, y: -20 }}
        animate={{ scale: 1, opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.1 }}
      >
        <Box className="logo-bg logo-bg--solid" sx={{ width: 200, height: 200, mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Box component="img" src={logo} alt="Gatherly logo" sx={{ width: 160, height: 160, objectFit: 'contain', display: 'block' }} onError={(e)=>{e.target.style.display='none'}} />
        </Box>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.2 }}
        style={{ width: '100%', maxWidth: 500 }}
      >
        <Card sx={{ maxWidth: 500, mx: 'auto' }}>
          <CardContent>
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: 0.3 }}
            >
              <Typography variant="h4" component="h1" align="center" gutterBottom>
                Welcome to Gatherly
              </Typography>
              <Typography variant="body2" align="center" color="text.secondary" sx={{ mb: 3 }}>
                Sign in or create an account to continue
              </Typography>
            </motion.div>
            
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
              <Tabs value={currentTab} onChange={handleTabChange} centered>
                <Tab label="Sign In" />
                <Tab label="Sign Up" />
              </Tabs>
            </Box>

            <AnimatePresence>
              {error && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  exit={{ opacity: 0, height: 0 }}
                  transition={{ duration: 0.2 }}
                >
                  <Alert severity="error" sx={{ mt: 2 }}>
                    {error}
                  </Alert>
                </motion.div>
              )}
            </AnimatePresence>

            <TabPanel value={currentTab} index={0}>
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.2 }}
              >
                <Box component="form" onSubmit={handleLogin}>
                  <motion.div
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.2, delay: 0.1 }}
                  >
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
                  </motion.div>
                  <motion.div
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.2, delay: 0.15 }}
                  >
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
                  </motion.div>
                  <motion.div
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.2, delay: 0.2 }}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                  >
                    <Button
                      type="submit"
                      fullWidth
                      variant="contained"
                      sx={{ mt: 3, mb: 2 }}
                      disabled={loading}
                    >
                      {loading ? <CircularProgress size={24} /> : 'Sign In'}
                    </Button>
                  </motion.div>
                </Box>
              </motion.div>
            </TabPanel>

            <TabPanel value={currentTab} index={1}>
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.2 }}
              >
                <Box component="form" onSubmit={handleRegister}>
                  <motion.div
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.2, delay: 0.1 }}
                  >
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
                  </motion.div>
                  <motion.div
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.2, delay: 0.13 }}
                  >
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
                  </motion.div>
                  <motion.div
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.2, delay: 0.16 }}
                  >
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
                  </motion.div>
                  <motion.div
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.2, delay: 0.19 }}
                  >
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
                  </motion.div>
                  <motion.div
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.2, delay: 0.22 }}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                  >
                    <Button
                      type="submit"
                      fullWidth
                      variant="contained"
                      sx={{ mt: 3, mb: 2 }}
                      disabled={loading}
                    >
                      {loading ? <CircularProgress size={24} /> : 'Sign Up'}
                    </Button>
                  </motion.div>
                </Box>
              </motion.div>
            </TabPanel>
          </CardContent>
        </Card>
      </motion.div>
    </Container>
  );
};

export default LoginComponent;
