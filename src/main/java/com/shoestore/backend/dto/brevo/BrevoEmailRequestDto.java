package com.shoestore.backend.dto.brevo;

import java.util.List;

public record BrevoEmailRequestDto(
        SenderDto sender,
        List<RecipientDto> to,
        String subject,
        String htmlContent
) {
}
