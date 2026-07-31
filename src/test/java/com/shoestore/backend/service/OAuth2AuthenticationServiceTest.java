package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shoestore.backend.dto.user.UserLoginResponseDto;
import com.shoestore.backend.exceptation.EntityNotFoundException;
import com.shoestore.backend.model.AuthProvider;
import com.shoestore.backend.model.Role;
import com.shoestore.backend.model.RoleName;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.RoleRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.impl.OAuth2AuthenticationServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OAuth2AuthenticationServiceTest {
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private OAuth2AuthenticationServiceImpl service;

    @Test
    void authenticateGoogleReturnsTokenForExistingUser() {
        User user = new User().setId(3L).setEmail("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(3L, "user@example.com")).thenReturn("oauth-token");

        UserLoginResponseDto response = service.authenticateGoogle(
                " USER@example.com ",
                "Ana",
                "Mills"
        );

        assertEquals("oauth-token", response.token());
    }

    @Test
    void authenticateGoogleCreatesUserWhenMissing() {
        Role role = new Role();
        role.setRoleName(RoleName.CUSTOMER);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleName.CUSTOMER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(null, "new@example.com")).thenReturn("created-token");

        UserLoginResponseDto response = service.authenticateGoogle(
                " NEW@example.com ",
                "New",
                "User"
        );

        assertEquals("created-token", response.token());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("new@example.com", userCaptor.getValue().getEmail());
        assertEquals(AuthProvider.GOOGLE, userCaptor.getValue().getAuthProvider());
        assertEquals(role, userCaptor.getValue().getRole());
    }

    @Test
    void authenticateGoogleThrowsWhenCustomerRoleIsMissing() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleName.CUSTOMER)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.authenticateGoogle(
                "new@example.com",
                "New",
                "User"
        ));
    }
}
