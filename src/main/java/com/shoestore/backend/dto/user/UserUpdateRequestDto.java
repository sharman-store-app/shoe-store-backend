package com.shoestore.backend.dto.user;

import com.shoestore.backend.validation.passwordvalidator.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatch(first = "password", second = "repeatedPassword", message = "Passwords do not match")
public record UserUpdateRequestDto(
        @Size(min = 3, message = "Name min size is 3 characters")
        String name,
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Current password cannot be blank")
        String currentPassword,
        @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
        String password,
        @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
        String repeatedPassword
) {
}
