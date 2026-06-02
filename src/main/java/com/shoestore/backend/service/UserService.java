package com.shoestore.backend.service;

import com.shoestore.backend.dto.user.UserRegistrationRequestDto;
import com.shoestore.backend.dto.user.UserResponseDto;
import com.shoestore.backend.dto.user.UserUpdateRequestDto;
import com.shoestore.backend.exceptation.RegistrationException;
import jakarta.validation.Valid;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto request) throws RegistrationException;

    UserResponseDto getCurrentUserData(String userEmail);

    UserResponseDto updateUserData(@Valid UserUpdateRequestDto request, String userEmail);
}
