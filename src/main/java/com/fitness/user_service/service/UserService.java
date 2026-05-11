package com.fitness.user_service.service;

import com.fitness.user_service.model.User;
import com.fitness.user_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserProfile(Integer userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }

    public Boolean getUserProfileIsValid(Integer userId) {

        return userRepository.existsById(userId);
    }
}
