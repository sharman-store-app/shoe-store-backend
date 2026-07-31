package com.shoestore.backend.service;

import com.shoestore.backend.dto.user.UserLoginRequestDto;
import com.shoestore.backend.dto.user.UserLoginResponseDto;

public interface AuthenticationService {

    UserLoginResponseDto authenticate(UserLoginRequestDto request);
}
