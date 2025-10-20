package com.screenshare.service;

import com.screenshare.dto.AuthResponse;
import com.screenshare.dto.LoginRequest;
import com.screenshare.dto.RegisterRequest;
import com.screenshare.entity.User;
import com.screenshare.entity.UserStatus;
import com.screenshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public AuthResponse register(RegisterRequest request) {
        try {
            // Validate passwords match
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return AuthResponse.error("Passwords do not match");
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return AuthResponse.error("Email already exists");
            }
            
            // Check if username already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                return AuthResponse.error("Username already exists");
            }
            
            // Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername());
            user.setStatus(UserStatus.OFFLINE);
            user.setIsActive(true);
            user.setLastSeenAt(LocalDateTime.now());
            
            // Save user to database
            User savedUser = userRepository.save(user);
            
            return AuthResponse.success("User registered successfully", savedUser);
            
        } catch (Exception e) {
            return AuthResponse.error("Registration failed: " + e.getMessage());
        }
    }
    
    public AuthResponse login(LoginRequest request) {
        try {
            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            
            if (userOptional.isEmpty()) {
                return AuthResponse.error("Invalid email or password");
            }
            
            User user = userOptional.get();
            
            // Check if user is active
            if (!user.getIsActive()) {
                return AuthResponse.error("Account is deactivated");
            }
            
            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return AuthResponse.error("Invalid email or password");
            }
            
            // Update last seen and status
            user.setLastSeenAt(LocalDateTime.now());
            user.setStatus(UserStatus.ONLINE);
            userRepository.save(user);
            
            return AuthResponse.success("Login successful", user);
            
        } catch (Exception e) {
            return AuthResponse.error("Login failed: " + e.getMessage());
        }
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}

