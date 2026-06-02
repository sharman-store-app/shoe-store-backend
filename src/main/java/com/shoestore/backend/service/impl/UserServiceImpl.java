package com.shoestore.backend.service.impl;

import com.shoestore.backend.dto.user.UserRegistrationRequestDto;
import com.shoestore.backend.dto.user.UserResponseDto;
import com.shoestore.backend.dto.user.UserUpdateRequestDto;
import com.shoestore.backend.exceptation.EntityNotFoundException;
import com.shoestore.backend.exceptation.InvalidPasswordException;
import com.shoestore.backend.exceptation.RegistrationException;
import com.shoestore.backend.mapper.UserMapper;
import com.shoestore.backend.model.Role;
import com.shoestore.backend.model.RoleName;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.RoleRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.service.UserService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
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

    @Override
    public UserResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RegistrationException("Email is taken");
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

    private User findUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail).orElseThrow(
                () -> new EntityNotFoundException("User with email '" + userEmail
                        + "' does not exist")
        );
    }
}
