package com.shoestore.backend.dto.user;

import java.time.Instant;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        Instant createdAt
) {
}
