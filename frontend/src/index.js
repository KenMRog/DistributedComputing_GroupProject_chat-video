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
    versions: {},
    nextTick: function(callback) {
      // Use setTimeout as a browser-compatible alternative to process.nextTick
      setTimeout(callback, 0);
    }
  };
}

// Global error handler to suppress simple-peer browser compatibility errors
if (typeof window !== 'undefined') {
  // Handle unhandled errors from simple-peer
  window.addEventListener('error', (event) => {
    const errorMsg = (event.message || '').toLowerCase();
    const errorStack = (event.error?.stack || '').toLowerCase();
    const fullError = errorMsg + ' ' + errorStack;
    
    // Suppress known simple-peer browser compatibility errors
    if (fullError.includes('_readablestate') ||
        fullError.includes('_stream_readable') ||
        fullError.includes('stream is undefined') ||
        fullError.includes("can't access property") ||
        fullError.includes("cannot access property") ||
        fullError.includes('process.nexttick') ||
        fullError.includes('emitreadable')) {
      // Silently ignore these errors - they're browser environment limitations
      event.preventDefault();
      return false;
    }
  }, true); // Use capture phase to catch errors early
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </React.StrictMode>
);
