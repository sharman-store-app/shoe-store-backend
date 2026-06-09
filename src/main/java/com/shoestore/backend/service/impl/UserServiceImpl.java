package com.shoestore.backend.service.impl;

import com.shoestore.backend.dto.user.ResetPasswordRequestDto;
import com.shoestore.backend.dto.user.UserForgotPasswordRequestDto;
import com.shoestore.backend.dto.user.UserRegistrationRequestDto;
import com.shoestore.backend.dto.user.UserResponseDto;
import com.shoestore.backend.dto.user.UserUpdateRequestDto;
import com.shoestore.backend.exceptation.BadCredentialsException;
import com.shoestore.backend.exceptation.EntityNotFoundException;
import com.shoestore.backend.exceptation.InvalidPasswordException;
import com.shoestore.backend.exceptation.RegistrationException;
import com.shoestore.backend.mapper.UserMapper;
import com.shoestore.backend.model.PasswordResetToken;
import com.shoestore.backend.model.Role;
import com.shoestore.backend.model.RoleName;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.PasswordResetTokenRepository;
import com.shoestore.backend.repository.RoleRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.service.EmailService;
import com.shoestore.backend.service.UserService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RegistrationException("An account with this email already exists. "
                    + "Please sign in or use a different email");
        }

        Role role = roleRepository.findByRoleName(RoleName.CUSTOMER).orElseThrow(
                () -> new EntityNotFoundException("Role Customer doesn't exist in database")
        );

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password())).setCreatedAt(Instant.now())
                .setRole(role);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getCurrentUserData(String userEmail) {
        User user = findUserByEmail(userEmail);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserData(UserUpdateRequestDto request, String userEmail) {
        User user = findUserByEmail(userEmail);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Password is incorrect");
        }
        userMapper.updateEntity(request, user);
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public void forgotPassword(UserForgotPasswordRequestDto request) {
        User user = findUserByEmail(request.email());
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken()
                .setUser(user)
                .setToken(token)
                .setExpiresAt(
                        Instant.now().plus(1, ChronoUnit.HOURS)
                );
        passwordResetTokenRepository.save(passwordResetToken);
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendEmail(
                user.getEmail(),
                "Password Reset",
                "Hello,\n\n"
                        + "Please click the following link to reset your password:\n\n"
                        + resetLink
                        + "\n\n"
                        + "This feature is currently under development, and the frontend reset "
                        + "password page is not available yet.\n\n"
        );
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByToken(request.token()).orElseThrow(() ->
                        new EntityNotFoundException("Token not found in database"));
        if (Instant.now().isAfter(passwordResetToken.getExpiresAt())) {
            throw new BadCredentialsException("Token is expired");
        }
        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(passwordResetToken);
    }

    private User findUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail.toLowerCase()).orElseThrow(
                () -> new EntityNotFoundException("No account found with  email " + userEmail
                        + ". Please create an account")
        );
    }
}
