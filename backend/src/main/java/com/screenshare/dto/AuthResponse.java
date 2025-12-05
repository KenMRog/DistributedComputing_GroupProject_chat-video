package com.screenshare.dto;

import com.screenshare.entity.User;

public class AuthResponse {
    
    private boolean success;
    private String message;
    private UserData user;
    private String token; 
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public AuthResponse(boolean success, String message, UserData user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public UserData getUser() {
        return user;
    }
    
    public void setUser(UserData user) {
        this.user = user;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
   
    public static AuthResponse success(String message, User user) {
        AuthResponse response = new AuthResponse(true, message);
        response.setUser(new UserData(user));
        return response;
    }
    
    public static AuthResponse error(String message) {
        return new AuthResponse(false, message);
    }
    
    // Inner class for user data
    public static class UserData {
        private Long id;
        private String username;
        private String email;
        private String displayName;
        private String status;
        private String createdAt;
        
        public UserData() {}
        
        public UserData(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.displayName = user.getDisplayName();
            this.status = user.getStatus().toString();
            this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
        }
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}

