package com.shoestore.backend.controller;

import com.shoestore.backend.dto.user.ResetPasswordRequestDto;
import com.shoestore.backend.dto.user.UserForgotPasswordRequestDto;
import com.shoestore.backend.dto.user.UserLoginRequestDto;
import com.shoestore.backend.dto.user.UserLoginResponseDto;
import com.shoestore.backend.dto.user.UserRegistrationRequestDto;
import com.shoestore.backend.dto.user.UserResponseDto;
import com.shoestore.backend.exceptation.RegistrationException;
import com.shoestore.backend.service.AuthenticationService;
import com.shoestore.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "01. Authentication",
        description = "Authentication related endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Allows users to register a new account")
    public UserResponseDto register(@Valid @RequestBody UserRegistrationRequestDto request)
            throws RegistrationException {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Grants JWT tokens to authenticated users")
    public UserLoginResponseDto login(@Valid @RequestBody UserLoginRequestDto request) {
        return authenticationService.authenticate(request);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Request password reset", description = "Sends a password reset link "
            + "to the registered email address")
    public void forgotPassword(@Valid @RequestBody UserForgotPasswordRequestDto request) {
        userService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reset password", description = "Resets the password using a valid "
            + "reset token")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        userService.resetPassword(request);
    }
}
