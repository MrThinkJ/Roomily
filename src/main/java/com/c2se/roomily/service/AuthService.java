package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.LoginRequest;
import com.c2se.roomily.payload.request.RegisterRequest;
import com.c2se.roomily.payload.response.LoginResponse;
import com.c2se.roomily.payload.response.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    void register(RegisterRequest registerRequest);
    UserResponse me(String userId);
}
