package com.shoestore.backend.dto.payment;

public record PaymentResponseDto(
        Long paymentId,
        String status,
        String message
) {
}
