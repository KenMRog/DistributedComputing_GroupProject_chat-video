import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import ErrorBoundary from './ErrorBoundary';

// Polyfill for Node.js 'process' global that simple-peer expects
// Create React App replaces process.env.NODE_ENV at build time via webpack DefinePlugin
if (typeof window !== 'undefined' && typeof process === 'undefined') {
  // eslint-disable-next-line no-undef
  window.process = {
    env: {
      NODE_ENV: process.env.NODE_ENV || 'development'
    },
    browser: true,
    version: '',
    versions: {}
  };
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </React.StrictMode>
);
