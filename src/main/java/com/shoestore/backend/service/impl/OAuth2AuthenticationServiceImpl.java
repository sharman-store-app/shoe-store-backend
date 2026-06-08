package com.shoestore.backend.service.impl;

import com.shoestore.backend.dto.user.UserLoginResponseDto;
import com.shoestore.backend.exceptation.EntityNotFoundException;
import com.shoestore.backend.model.AuthProvider;
import com.shoestore.backend.model.Role;
import com.shoestore.backend.model.RoleName;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.RoleRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.OAuth2AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2AuthenticationServiceImpl implements OAuth2AuthenticationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public UserLoginResponseDto authenticateGoogle(
            String email, String firstName, String lastName) {

        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElseGet(() ->
                createGoogleUser(email.trim().toLowerCase(), firstName, lastName));

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return new UserLoginResponseDto(token);
    }

    private User createGoogleUser(String email, String firstName, String lastName) {
        Role role = roleRepository.findByRoleName(RoleName.CUSTOMER).orElseThrow(
                () -> new EntityNotFoundException("Role Customer doesn't exist in database")
        );
        return userRepository.save(new User().setEmail(email).setFirstName(firstName)
                .setLastName(lastName).setAuthProvider(AuthProvider.GOOGLE).setRole(role));
    }

}
