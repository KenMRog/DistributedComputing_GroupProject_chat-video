package com.screenshare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * Security Configuration
 * Disables authentication to allow open access to the application
 * Used for development and testing purposes
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS so the CorsConfigurationSource bean is used
            .cors().and()

            // Disable CSRF protection 
            .csrf(AbstractHttpConfigurer::disable)
            
            // Allow all requests without authentication
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            
            // Disable HTTP Basic authentication
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // Disable form login
            .formLogin(AbstractHttpConfigurer::disable);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}



