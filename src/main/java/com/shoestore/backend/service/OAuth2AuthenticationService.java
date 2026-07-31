package com.shoestore.backend.service;

import com.shoestore.backend.dto.user.UserLoginResponseDto;

public interface OAuth2AuthenticationService {

    UserLoginResponseDto authenticateGoogle(String email, String firstName, String lastName);
}
