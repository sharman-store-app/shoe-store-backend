package com.shoestore.backend.dto.user;

import com.shoestore.backend.validation.passwordvalidator.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatch(first = "password", second = "repeatedPassword", message = "Passwords do not match")
public record UserRegistrationRequestDto(
        @NotBlank(message = "Name cannot be blank")
        String name,
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email cannot be blank")
        String email,
        @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
        String password,
        @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
        String repeatedPassword
) {
}
