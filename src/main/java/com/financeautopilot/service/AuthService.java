package com.financeautopilot.service;

import com.financeautopilot.dto.request.LoginRequest;
import com.financeautopilot.dto.request.RegisterRequest;
import com.financeautopilot.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
