package com.fitness.user_service.exceptions;

import com.fitness.user_service.dto.ErrorDTO;
import com.fitness.user_service.dto.GlobalResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleSecurityException(Exception exception) {
        ErrorDTO error = null;

        switch (exception) {
            case JwtAuthenticationException jwtAuthenticationException -> {
                error = ErrorDTO.of("Authentication Failed", jwtAuthenticationException.getMessage());
                return ResponseEntity.status(401).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 401));
            }
            case EntityNotFoundException entityNotFoundException -> {
                error = ErrorDTO.of("Not Found", entityNotFoundException.getMessage());
                return ResponseEntity.status(404).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 404));
            }
            case IllegalArgumentException illegalArgumentException -> {
                error = ErrorDTO.of("Bad Request", "The request is invalid");
                return ResponseEntity.status(400).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 400));
            }
            case RuntimeException runtimeException -> {
                error = ErrorDTO.of("Something went wrong", runtimeException.getMessage());
                return ResponseEntity.status(500).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 500));
            }
            case null, default -> {
                assert exception != null;
                System.out.println("assdcasc");
                error = ErrorDTO.of("Internal Server Error", exception.getMessage());
            }
        }

        return ResponseEntity.status(500).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 500));
    }
}


