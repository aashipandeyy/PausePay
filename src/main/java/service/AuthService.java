package service;

import dto.request.LoginRequest;
import dto.request.RegisterRequest;
import dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
