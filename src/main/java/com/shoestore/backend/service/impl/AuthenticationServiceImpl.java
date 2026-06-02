package com.shoestore.backend.service.impl;

import com.shoestore.backend.dto.user.UserLoginRequestDto;
import com.shoestore.backend.dto.user.UserLoginResponseDto;
import com.shoestore.backend.exceptation.EntityNotFoundException;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Override
    public UserLoginResponseDto authenticate(UserLoginRequestDto request) {

        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        request.email().trim().toLowerCase(),
                        request.password()));

        User user = userRepository.findByEmail(authenticate.getName()).orElseThrow(
                () -> new EntityNotFoundException("User not found in database"));

        String token = jwtUtil.generateToken(user.getId(), authenticate.getName());

        return new UserLoginResponseDto(token);
    }
}
