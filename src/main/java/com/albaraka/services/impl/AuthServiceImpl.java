package com.albaraka.services.impl;

import com.albaraka.dto.LoginRequest;
import com.albaraka.dto.LoginResponse;
import com.albaraka.enums.Role;
import com.albaraka.exceptions.UnauthorizedAccessException;
import com.albaraka.models.User;
import com.albaraka.repositories.UserRepository;
import com.albaraka.services.interfaces.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedAccessException("Invalid email or password"));
        
        if (!user.getActive()) {
            throw new UnauthorizedAccessException("Account is deactivated");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedAccessException("Invalid email or password");
        }
        
        // TODO: Generate JWT token - will be implemented in security configuration
        String token = "JWT_TOKEN_PLACEHOLDER";
        
        return new LoginResponse(token, user.getEmail(), user.getRole(), user.getId());
    }
}

