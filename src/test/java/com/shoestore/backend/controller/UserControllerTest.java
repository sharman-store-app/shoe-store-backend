package com.shoestore.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoestore.backend.dto.user.UserResponseDto;
import com.shoestore.backend.dto.user.UserUpdateRequestDto;
import com.shoestore.backend.model.RoleName;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.UserService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getUserDataReturnsCurrentUser() throws Exception {
        UserResponseDto response = userResponse();
        when(userService.getCurrentUserData("john@example.com")).thenReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).getCurrentUserData("john@example.com");
    }

    @Test
    void updateUserDataReturnsUpdatedUser() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto(
                "Jane",
                "Doe",
                "jane@example.com",
                "+48123456789",
                "Password123!",
                "Password456#",
                "Password456#"
        );
        UserResponseDto response = new UserResponseDto(
                1L,
                "Jane",
                "Doe",
                "+48123456789",
                "jane@example.com",
                RoleName.CUSTOMER,
                Instant.parse("2026-01-01T00:00:00Z")
        );
        when(userService.updateUserData(any(UserUpdateRequestDto.class), eq("john@example.com")))
                .thenReturn(response);

        mockMvc.perform(patch("/api/users/me")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@example.com"));

        verify(userService).updateUserData(any(UserUpdateRequestDto.class), eq("john@example.com"));
    }

    @Test
    void updateUserDataReturnsBadRequestForInvalidEmail() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto(
                "Jane",
                "Doe",
                "bad-email",
                "+48123456789",
                null,
                null,
                null
        );

        mockMvc.perform(patch("/api/users/me")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private Authentication authentication() {
        return new TestingAuthenticationToken(
                "john@example.com",
                "password",
                "ROLE_CUSTOMER"
        );
    }

    private UserResponseDto userResponse() {
        return new UserResponseDto(
                1L,
                "John",
                "Doe",
                "+48123456789",
                "john@example.com",
                RoleName.CUSTOMER,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
