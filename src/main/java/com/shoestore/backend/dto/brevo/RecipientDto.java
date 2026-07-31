package com.shoestore.backend.dto.brevo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record RecipientDto(
        @Size(min = 6, max = 72, message = "Email must be between 6 and 72 characters")
        @Email(message = "Invalid email format")
        String email
) {
}
