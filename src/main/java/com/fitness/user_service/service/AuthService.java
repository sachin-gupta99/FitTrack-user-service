package com.fitness.user_service.service;

import com.fitness.user_service.config.RabbitMQConfig;
import com.fitness.user_service.dto.AuthResponseDTO;
import com.fitness.user_service.dto.LoginRequestDTO;
import com.fitness.user_service.dto.UserRegisterRequestDTO;
import com.fitness.user_service.dto.UserSavedDTO;
import com.fitness.user_service.model.User;
import com.fitness.user_service.model.UserRole;
import com.fitness.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RabbitTemplate rabbitTemplate;

    public AuthResponseDTO register(UserRegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);

        UserSavedDTO userSavedDTO = UserSavedDTO.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole().name())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();

        System.out.println("User registered: " + userSavedDTO);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.USER_ROUTING_KEY,
                userSavedDTO
        );

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());

        String token = jwtService.generateToken(user, extraClaims);

        return AuthResponseDTO.builder()
                .token(token)
                .email(user.getEmail())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());

        String token = jwtService.generateToken(user, extraClaims);

        return AuthResponseDTO.builder()
                .token(token)
                .email(user.getEmail())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponseDTO validateToken(String token) {
        Boolean isTokenValid = jwtService.isTokenValid(token);
        if (!isTokenValid) {
            throw new RuntimeException("Invalid token");
        }

        String userEmail = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found for token");
        }

        return AuthResponseDTO.builder()
                .email(user.getEmail())
                .token(token)
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }
}

