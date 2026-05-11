package com.fitness.user_service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserSavedDTO {

    Integer id;
    String firstName;
    String lastName;
    String email;
    String password;
    String role;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
