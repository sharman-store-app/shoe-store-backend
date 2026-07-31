package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shoestore.backend.dto.user.UserLoginRequestDto;
import com.shoestore.backend.dto.user.UserLoginResponseDto;
import com.shoestore.backend.exceptation.EntityNotFoundException;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.impl.AuthenticationServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private AuthenticationServiceImpl service;

    @Test
    void authenticateReturnsJwtForExistingUser() {
        final UserLoginRequestDto request = new UserLoginRequestDto("  USER@Example.COM ",
                "Password123!"
        );
        User user = new User().setId(7L).setEmail("user@example.com");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(7L, "user@example.com")).thenReturn("jwt-token");

        UserLoginResponseDto response = service.authenticate(request);

        assertEquals("jwt-token", response.token());
    }

    @Test
    void authenticateThrowsWhenAuthenticatedEmailHasNoUser() {
        final UserLoginRequestDto request = new UserLoginRequestDto("user@example.com",
                "Password12!"
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.authenticate(request));
    }
}
