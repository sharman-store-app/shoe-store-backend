package com.shoestore.backend.controller;

import com.shoestore.backend.dto.session.SessionRequestDto;
import com.shoestore.backend.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "06. Sessions", description = "Session endpoint")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @Operation(summary = "Create visitor session", description = "Post session data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createSession(@RequestBody SessionRequestDto request,
                              Authentication authentication,
                              HttpServletRequest httpServletRequest) {
        sessionService.createSession(request, authentication, httpServletRequest);
    }
}
