package com.shoestore.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoestore.backend.dto.user.ResetPasswordRequestDto;
import com.shoestore.backend.dto.user.UserForgotPasswordRequestDto;
import com.shoestore.backend.dto.user.UserLoginRequestDto;
import com.shoestore.backend.dto.user.UserLoginResponseDto;
import com.shoestore.backend.dto.user.UserRegistrationRequestDto;
import com.shoestore.backend.dto.user.UserResponseDto;
import com.shoestore.backend.model.RoleName;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.AuthenticationService;
import com.shoestore.backend.service.UserService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AuthenticationService authenticationService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void registerReturnsCreatedUser() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "John",
                "Doe",
                "john@example.com",
                "+48123456789",
                "Password123!",
                "Password123!"
        );
        UserResponseDto response = new UserResponseDto(
                1L,
                "John",
                "Doe",
                "+48123456789",
                "john@example.com",
                RoleName.CUSTOMER,
                Instant.parse("2026-01-01T00:00:00Z")
        );
        when(userService.register(any(UserRegistrationRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).register(any(UserRegistrationRequestDto.class));
    }

    @Test
    void loginReturnsToken() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "john@example.com",
                "Password123!"
        );
        when(authenticationService.authenticate(any(UserLoginRequestDto.class)))
                .thenReturn(new UserLoginResponseDto("jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authenticationService).authenticate(any(UserLoginRequestDto.class));
    }

    @Test
    void loginReturnsBadRequestForInvalidEmail() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto("bad-email", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPasswordDelegatesToService() throws Exception {
        UserForgotPasswordRequestDto request =
                new UserForgotPasswordRequestDto("john@example.com");
        doNothing().when(userService).forgotPassword(any(UserForgotPasswordRequestDto.class));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).forgotPassword(any(UserForgotPasswordRequestDto.class));
    }

    @Test
    void resetPasswordDelegatesToService() throws Exception {
        ResetPasswordRequestDto request =
                new ResetPasswordRequestDto("reset-token", "Password456#");
        doNothing().when(userService).resetPassword(any(ResetPasswordRequestDto.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).resetPassword(any(ResetPasswordRequestDto.class));
    }
}
