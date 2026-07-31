package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shoestore.backend.dto.brevo.BrevoEmailRequestDto;
import com.shoestore.backend.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestBodyUriSpec uriSpec;
    @Mock
    private RestClient.RequestBodySpec bodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;
    @InjectMocks
    private EmailServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "apiKey", "api-key");
        ReflectionTestUtils.setField(service, "mailFrom", "noreply@example.com");
    }

    @Test
    void sendEmailPostsBrevoRequestWithHtmlContent() {
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("https://api.brevo.com/v3/smtp/email")).thenReturn(bodySpec);
        when(bodySpec.header("api-key", "api-key")).thenReturn(bodySpec);
        when(bodySpec.header("Content-Type", "application/json")).thenReturn(bodySpec);
        when(bodySpec.body(any(BrevoEmailRequestDto.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);

        service.sendEmail("user@example.com", "Subject", "Line one\nLine two");

        ArgumentCaptor<BrevoEmailRequestDto> requestCaptor =
                ArgumentCaptor.forClass(BrevoEmailRequestDto.class);
        verify(bodySpec).body(requestCaptor.capture());
        assertEquals("Shoe Store", requestCaptor.getValue().sender().name());
        assertEquals("noreply@example.com", requestCaptor.getValue().sender().email());
        assertEquals("user@example.com", requestCaptor.getValue().to().get(0).email());
        assertEquals("Subject", requestCaptor.getValue().subject());
        assertEquals("Line one<br>Line two", requestCaptor.getValue().htmlContent());
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void sendEmailWrapsRestClientFailure() {
        when(restClient.post()).thenThrow(new RuntimeException("network"));

        assertThrows(IllegalStateException.class, () ->
                service.sendEmail("user@example.com", "Subject", "Content"));
    }
}
