package com.fitness.user_service.controllers;

import com.fitness.user_service.dto.AuthResponseDTO;
import com.fitness.user_service.dto.GlobalResponseDTO;
import com.fitness.user_service.dto.LoginRequestDTO;
import com.fitness.user_service.dto.UserRegisterRequestDTO;
import com.fitness.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> register(
            @Valid @RequestBody UserRegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.ok(GlobalResponseDTO.success(response, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(GlobalResponseDTO.success(response, "Login successful"));
    }

    @GetMapping("/validateToken")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        // Extract token from "Bearer <token>" format
        String token = authHeader.substring(7);
        AuthResponseDTO isValid = authService.validateToken(token);
        return ResponseEntity.ok(GlobalResponseDTO.success(isValid, "Token validation successful"));
    }
}

