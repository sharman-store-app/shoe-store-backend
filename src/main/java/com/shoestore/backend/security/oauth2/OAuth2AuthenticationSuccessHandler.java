package com.shoestore.backend.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoestore.backend.dto.user.UserLoginResponseDto;
import com.shoestore.backend.service.OAuth2AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${frontend.url}")
    String frontendUrl;
    private final OAuth2AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");

        UserLoginResponseDto responseDto =
                authenticationService.authenticateGoogle(email, firstName, lastName);
        String encodedJwt = URLEncoder.encode(responseDto.token(), StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(
                request,
                response,
                frontendUrl + "/oauth2/callback?token=" + encodedJwt
        );
    }
}
