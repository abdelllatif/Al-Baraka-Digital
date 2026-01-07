package com.albaraka.services.impl;

import com.albaraka.config.CustomUserDetails;
import com.albaraka.config.JwtUtil;
import com.albaraka.config.UserDetailsServiceImpl;
import com.albaraka.dto.LoginRequest;
import com.albaraka.dto.LoginResponse;
import com.albaraka.enums.Role;
import com.albaraka.exceptions.UnauthorizedAccessException;
import com.albaraka.models.User;
import com.albaraka.repositories.UserRepository;
import com.albaraka.services.interfaces.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsServiceImpl;
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
        CustomUserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generateToken(userDetails,
                userDetails.getId(),
                userDetails.getRole());
        
        return new LoginResponse(token, user.getEmail(), user.getRole(), user.getId());
    }
}

