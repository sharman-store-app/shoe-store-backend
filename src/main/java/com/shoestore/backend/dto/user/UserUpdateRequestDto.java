package com.shoestore.backend.dto.user;

import com.shoestore.backend.validation.passwordvalidator.PasswordMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordMatch(first = "password", second = "repeatedPassword", message = "Passwords do not match")
public record UserUpdateRequestDto(
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        @Pattern(regexp = "^[\\p{IsLatin}\\p{IsCyrillic}]+$",
                message = "First name may contain only Latin and Cyrillic letters"
        )
        @Schema(example = "John")
        String firstName,
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        @Pattern(
                regexp = "^[\\p{IsLatin}\\p{IsCyrillic}]+$",
                message = "Last name may contain only Latin and Cyrillic letters"
        )
        @Schema(example = "Doe")
        String lastName,
        @Size(min = 6, max = 72, message = "Email must be between 6 and 72 characters")
        @Email(message = "Invalid email format")
        String email,
        @Pattern(
                regexp = "^\\+[0-9 ]+$",
                message = "Phone number must start with '+' and contain only digits"
        )
        @Schema(example = "+48123456789")
        String phoneNumber,
        @Schema(example = "Password123!")
        String currentPassword,
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,64}$",
                message = "Password must be 8-64 characters long and contain at least one "
                        + "uppercase letter, one lowercase letter, one digit, and one special "
                        + "character"
        )
        @Schema(example = "Password456#")
        String password,
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,64}$",
                message = "Password must be 8-64 characters long and contain at least one "
                        + "uppercase letter, one lowercase letter, one digit, and one special "
                        + "character"
        )
        @Schema(example = "Password456#")
        String repeatedPassword
) {
}
