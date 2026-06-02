package com.shoestore.backend.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @NotBlank(message = "Cannot be empty")
        String email,
        @NotBlank(message = "Cannot be empty")
        String password) {
}
