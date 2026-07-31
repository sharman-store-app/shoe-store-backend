package com.shoestore.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @NotBlank(message = "Email is required")
        @Size(min = 6, max = 72, message = "Email must be between 6 and 72 characters")
        @Email(message = "Invalid email format")
        String email,
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,64}$",
                message = "Password must be 8-64 characters long and contain at least one "
                        + "uppercase letter, one lowercase letter, one digit, and one special "
                        + "character"
        )
        @Schema(example = "Password123!")
        String password) {
}
