package com.shoestore.backend.service;

import com.shoestore.backend.dto.user.ResetPasswordRequestDto;
import com.shoestore.backend.dto.user.UserForgotPasswordRequestDto;
import com.shoestore.backend.dto.user.UserRegistrationRequestDto;
import com.shoestore.backend.dto.user.UserResponseDto;
import com.shoestore.backend.dto.user.UserUpdateRequestDto;
import com.shoestore.backend.exceptation.RegistrationException;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto request) throws RegistrationException;

    UserResponseDto getCurrentUserData(String userEmail);

    UserResponseDto updateUserData(UserUpdateRequestDto request, String userEmail);

    void forgotPassword(UserForgotPasswordRequestDto request);

    void resetPassword(ResetPasswordRequestDto request);
}
