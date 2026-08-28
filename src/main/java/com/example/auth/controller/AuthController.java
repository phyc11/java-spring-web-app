package com.example.auth.controller;

import com.example.common.dto.ApiResponse;
import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.UserDTO;
import com.example.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.ok("Login successful", response);
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResponse.ok("User registered successfully", response);
    }

    @GetMapping("/me")
    public ApiResponse<UserDTO> getMe(Principal principal) {
        if (principal == null) {
            return ApiResponse.error("Not authenticated");
        }
        UserDTO user = authService.getCurrentUser(principal.getName());
        return ApiResponse.ok("Current user retrieved", user);
    }
}
