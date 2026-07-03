package com.shoestore.backend.controller;

import com.shoestore.backend.dto.user.UserResponseDto;
import com.shoestore.backend.dto.user.UserUpdateRequestDto;
import com.shoestore.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "02. Users", description = "User related endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get logged in user info",
            description = "Get current user information")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public UserResponseDto getUserData(Authentication authentication) {
        String userEmail = authentication.getName();
        return userService.getCurrentUserData(userEmail);
    }

    @PatchMapping("/me")
    @Operation(summary = "Update logged in user info",
            description = "Update current user information")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public UserResponseDto updateUserData(Authentication authentication,
                                   @RequestBody @Valid UserUpdateRequestDto request) {
        String userEmail = authentication.getName();
        return userService.updateUserData(request, userEmail);
    }
}
