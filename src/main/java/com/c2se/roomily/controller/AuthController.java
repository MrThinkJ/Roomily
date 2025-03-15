package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.LoginRequest;
import com.c2se.roomily.payload.request.RegisterRequest;
import com.c2se.roomily.payload.response.LoginResponse;
import com.c2se.roomily.payload.response.UserResponse;
import com.c2se.roomily.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController{
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(authService.me(userId));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok().build();
    }
}
