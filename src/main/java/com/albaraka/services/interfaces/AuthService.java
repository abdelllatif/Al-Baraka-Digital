package com.albaraka.services.interfaces;

import com.albaraka.dto.LoginRequest;
import com.albaraka.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(String email, String password);
}

