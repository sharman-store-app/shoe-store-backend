package com.shoestore.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequestDto(
        @NotNull @NotBlank(message = "Token must be field")
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        String token,
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,64}$",
                message = "Password must be 8-64 characters long and contain at least one "
                        + "uppercase letter, one lowercase letter, one digit, and one special "
                        + "character"
        )
        @NotBlank(message = "Password cannot be blank")
        @Schema(example = "Password123!")
        String newPassword
) {
}
