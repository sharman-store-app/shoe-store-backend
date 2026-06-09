package com.shoestore.backend.service.impl;

import com.shoestore.backend.dto.brevo.BrevoEmailRequestDto;
import com.shoestore.backend.dto.brevo.RecipientDto;
import com.shoestore.backend.dto.brevo.SenderDto;
import com.shoestore.backend.service.EmailService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final RestClient restClient;

    @Value("${brevo.api.key}")
    private String apiKey;
    @Value("${mail.from}")
    private String mailFrom;

    @Override
    public void sendEmail(String to, String subject, String content) {

        BrevoEmailRequestDto request = new BrevoEmailRequestDto(
                new SenderDto("Shoe Store", mailFrom),
                List.of(new RecipientDto(to)),
                subject,
                content.replace("\n", "<br>")
        );

        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send email via Brevo", e);
        }
    }
}
