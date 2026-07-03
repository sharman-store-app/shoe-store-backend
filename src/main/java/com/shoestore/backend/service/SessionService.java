package com.shoestore.backend.service;

import com.shoestore.backend.dto.session.SessionRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface SessionService {
    void createSession(SessionRequestDto request, Authentication authentication,
                       HttpServletRequest httpServletRequest);
}
