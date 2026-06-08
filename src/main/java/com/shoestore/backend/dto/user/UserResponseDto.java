package com.shoestore.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record UserResponseDto(
        Long id,
        @Schema(example = "John")
        String firstName,
        @Schema(example = "Doe")
        String lastName,
        @Schema(example = "+48123456789")
        String phoneNumber,
        @Schema(example = "user@example.com")
        String email,
        Instant createdAt
) {
}
