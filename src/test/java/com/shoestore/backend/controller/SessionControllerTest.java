package com.shoestore.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoestore.backend.dto.session.SessionRequestDto;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private SessionService sessionService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void createSessionReturnsNoContent() throws Exception {
        SessionRequestDto request = new SessionRequestDto("google", "PL");
        doNothing().when(sessionService).createSession(
                any(SessionRequestDto.class),
                nullable(Authentication.class),
                any(HttpServletRequest.class)
        );

        mockMvc.perform(post("/api/sessions")
                        .with(user("john@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(sessionService).createSession(
                any(SessionRequestDto.class),
                nullable(Authentication.class),
                any(HttpServletRequest.class)
        );
    }
}
