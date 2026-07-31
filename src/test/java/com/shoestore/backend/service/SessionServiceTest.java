package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shoestore.backend.dto.session.SessionRequestDto;
import com.shoestore.backend.model.Channel;
import com.shoestore.backend.model.Country;
import com.shoestore.backend.model.DeviceType;
import com.shoestore.backend.model.Session;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.CountryRepository;
import com.shoestore.backend.repository.SessionRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.service.impl.SessionServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private HttpServletRequest httpServletRequest;
    @InjectMocks
    private SessionServiceImpl service;

    @Test
    void createSessionStoresOrganicMobileChromeSessionForAuthenticatedUser() {
        User user = new User().setEmail("user@example.com");
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(httpServletRequest.getHeader("User-Agent"))
                .thenReturn("Mozilla/5.0 Android Chrome/120");
        when(countryRepository.findById("PL")).thenReturn(Optional.of(country("Poland")));

        service.createSession(
                new SessionRequestDto("https://www.google.com/search?q=shoes", "PL"),
                authentication,
                httpServletRequest
        );

        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        assertEquals(user, sessionCaptor.getValue().getUser());
        assertEquals("Chrome", sessionCaptor.getValue().getBrowser());
        assertEquals(DeviceType.MOBILE, sessionCaptor.getValue().getDeviceType());
        assertEquals(Channel.ORGANIC, sessionCaptor.getValue().getChannel());
        assertEquals("Poland", sessionCaptor.getValue().getCountry());
    }

    @Test
    void createSessionUsesDirectDesktopDefaultsForInvalidReferrerAndNoUserAgent() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(null);
        when(countryRepository.findById("PL")).thenReturn(Optional.of(country("Poland")));

        service.createSession(
                new SessionRequestDto("not a url", "PL"),
                null,
                httpServletRequest
        );

        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        assertNull(sessionCaptor.getValue().getUser());
        assertEquals("Unknown", sessionCaptor.getValue().getBrowser());
        assertEquals(DeviceType.DESKTOP, sessionCaptor.getValue().getDeviceType());
        assertEquals(Channel.DIRECT, sessionCaptor.getValue().getChannel());
    }

    @Test
    void createSessionThrowsWhenCountryMissing() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(null);
        when(countryRepository.findById("XX")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.createSession(
                new SessionRequestDto(null, "XX"),
                null,
                httpServletRequest
        ));
    }

    private Country country(String name) {
        Country country = new Country();
        ReflectionTestUtils.setField(country, "name", name);
        return country;
    }
}
