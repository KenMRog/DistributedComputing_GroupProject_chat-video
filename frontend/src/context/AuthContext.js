import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in from localStorage
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (error) {
        console.error('Error parsing saved user:', error);
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();

      if (data.success) {
        const userData = {
          id: data.user.id,
          email: data.user.email,
          username: data.user.username,
          name: data.user.displayName || data.user.username,
          status: data.user.status,
          createdAt: data.user.createdAt
        };
        
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
        return { success: true };
      } else {
        return { success: false, error: data.message };
      }
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, error: 'Login failed. Please try again.' };
    }
  };

  const register = async (email, password, confirmPassword, username) => {
    try {
      if (!email || !password || !confirmPassword) {
        return { success: false, error: 'All fields are required' };
      }
      
      if (password !== confirmPassword) {
        return { success: false, error: 'Passwords do not match' };
      }
      
      if (password.length < 6) {
        return { success: false, error: 'Password must be at least 6 characters' };
      }

      // Use provided username or generate from email
      const finalUsername = username || email.split('@')[0];
      
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          username: finalUsername, 
          email, 
          password, 
          confirmPassword,
          displayName: finalUsername
        }),
      });

      const data = await response.json();

      if (data.success) {
        const userData = {
          id: data.user.id,
          email: data.user.email,
          username: data.user.username,
          name: data.user.displayName || data.user.username,
          status: data.user.status,
          createdAt: data.user.createdAt
        };
        
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
        return { success: true };
      } else {
        return { success: false, error: data.message };
      }
    } catch (error) {
      console.error('Registration error:', error);
      return { success: false, error: 'Registration failed. Please try again.' };
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
  };

  const value = {
    user,
    login,
    register,
    logout,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
