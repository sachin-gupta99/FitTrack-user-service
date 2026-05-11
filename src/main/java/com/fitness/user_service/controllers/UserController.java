package com.fitness.user_service.controllers;

import com.fitness.user_service.dto.GlobalResponseDTO;
import com.fitness.user_service.model.User;
import com.fitness.user_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<GlobalResponseDTO<User>> getUserProfile(@PathVariable Integer userId) {

        return ResponseEntity.ok(GlobalResponseDTO.success(userService.getUserProfile(userId), "User profile retrieved successfully"));
    }

    @GetMapping("/{userId}/validate")
    public ResponseEntity<Boolean> getUserProfileIsValid(@PathVariable Integer userId) {

        return ResponseEntity.ok(userService.getUserProfileIsValid(userId));
    }
}
