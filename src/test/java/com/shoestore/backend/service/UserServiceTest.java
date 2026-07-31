package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shoestore.backend.dto.user.ResetPasswordRequestDto;
import com.shoestore.backend.dto.user.UserForgotPasswordRequestDto;
import com.shoestore.backend.dto.user.UserRegistrationRequestDto;
import com.shoestore.backend.dto.user.UserResponseDto;
import com.shoestore.backend.dto.user.UserUpdateRequestDto;
import com.shoestore.backend.exceptation.BadCredentialsException;
import com.shoestore.backend.exceptation.InvalidPasswordException;
import com.shoestore.backend.exceptation.RegistrationException;
import com.shoestore.backend.mapper.UserMapper;
import com.shoestore.backend.model.AuthProvider;
import com.shoestore.backend.model.PasswordResetToken;
import com.shoestore.backend.model.Role;
import com.shoestore.backend.model.RoleName;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.PasswordResetTokenRepository;
import com.shoestore.backend.repository.RoleRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.service.impl.UserServiceImpl;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "frontendUrl", "https://shop.example");
    }

    @Test
    void registerCreatesLocalCustomer() {
        UserRegistrationRequestDto request = registrationRequest();
        Role role = new Role();
        role.setRoleName(RoleName.CUSTOMER);
        User mappedUser = new User().setEmail(request.email());
        User savedUser = new User().setId(1L).setEmail(request.email()).setRole(role);
        UserResponseDto dto = new UserResponseDto(
                1L,
                "Ann",
                "Lee",
                "+48123456789",
                request.email(),
                RoleName.CUSTOMER,
                Instant.now()
        );
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleName.CUSTOMER)).thenReturn(Optional.of(role));
        when(userMapper.toEntity(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(userRepository.save(mappedUser)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(dto);

        UserResponseDto response = service.register(request);

        assertEquals(dto, response);
        assertEquals("encoded", mappedUser.getPassword());
        assertEquals(role, mappedUser.getRole());
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        UserRegistrationRequestDto request = registrationRequest();
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(new User()));

        assertThrows(RegistrationException.class, () -> service.register(request));
        verifyNoInteractions(roleRepository, passwordEncoder);
    }

    @Test
    void updateUserDataRequiresCurrentPasswordForLocalUser() {
        User user = new User()
                .setEmail("user@example.com")
                .setAuthProvider(AuthProvider.LOCAL)
                .setPassword("encoded");
        UserUpdateRequestDto request = new UserUpdateRequestDto(
                "Ann",
                null,
                null,
                null,
                null,
                null,
                null
        );
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidPasswordException.class, () ->
                service.updateUserData(request, "USER@example.com"));
    }

    @Test
    void updateUserDataUpdatesAndEncodesNewPassword() {
        User user = new User()
                .setEmail("user@example.com")
                .setAuthProvider(AuthProvider.LOCAL)
                .setPassword("old");
        UserUpdateRequestDto request = new UserUpdateRequestDto(
                "Ann",
                "Lee",
                null,
                null,
                "Password123!",
                "Password456#",
                "Password456#"
        );
        UserResponseDto dto = new UserResponseDto(
                2L,
                "Ann",
                "Lee",
                null,
                "user@example.com",
                RoleName.CUSTOMER,
                null
        );
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "old")).thenReturn(true);
        when(passwordEncoder.encode("Password456#")).thenReturn("new-encoded");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        UserResponseDto response = service.updateUserData(request, "user@example.com");

        assertEquals(dto, response);
        assertEquals("new-encoded", user.getPassword());
        verify(userMapper).updateEntity(request, user);
    }

    @Test
    void forgotPasswordStoresTokenAndSendsResetEmail() {
        User user = new User().setEmail("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        service.forgotPassword(new UserForgotPasswordRequestDto("user@example.com"));

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        assertEquals(user, tokenCaptor.getValue().getUser());
        verify(emailService).sendEmail(
                eq("user@example.com"),
                eq("Password Reset"),
                contains("https://shop.example/reset-password")
        );
    }

    @Test
    void resetPasswordUpdatesPasswordAndDeletesToken() {
        User user = new User().setPassword("old");
        PasswordResetToken token = new PasswordResetToken()
                .setUser(user)
                .setToken("token")
                .setExpiresAt(Instant.now().plusSeconds(60));
        when(passwordResetTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("Password456#")).thenReturn("encoded");

        service.resetPassword(new ResetPasswordRequestDto("token", "Password456#"));

        assertEquals("encoded", user.getPassword());
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(token);
    }

    @Test
    void resetPasswordThrowsWhenTokenExpired() {
        PasswordResetToken token = new PasswordResetToken()
                .setExpiresAt(Instant.now().minusSeconds(1));
        when(passwordResetTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(token));

        assertThrows(BadCredentialsException.class, () ->
                service.resetPassword(new ResetPasswordRequestDto("token", "Password456#")));
    }

    private UserRegistrationRequestDto registrationRequest() {
        return new UserRegistrationRequestDto(
                "Ann",
                "Lee",
                "user@example.com",
                "+48123456789",
                "Password123!",
                "Password123!"
        );
    }
}
