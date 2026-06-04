package com.shoestore.backend.dto.user;

import com.shoestore.backend.validation.passwordvalidator.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@PasswordMatch(first = "password", second = "repeatedPassword", message = "Passwords do not match")
public record UserRegistrationRequestDto(
        @NotBlank(message = "Name cannot be blank")
        String name,
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email cannot be blank")
        String email,
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,64}$",
                message = "Password must be 8-64 characters long and contain at least one "
                        + "uppercase letter, one lowercase letter, one digit, and one special "
                        + "character"
        )
        String password,
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,64}$",
                message = "Password must be 8-64 characters long and contain at least one "
                        + "uppercase letter, one lowercase letter, one digit, and one special "
                        + "character"
        )
        String repeatedPassword
) {
}
