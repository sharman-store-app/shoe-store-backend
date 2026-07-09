package com.shoestore.backend.service;

import com.shoestore.backend.dto.payment.CreatePaymentRequestDto;
import com.shoestore.backend.dto.payment.PaymentDto;

public interface PaymentService {

    PaymentDto createPaymentSession(CreatePaymentRequestDto request);

    void handleWebhook(String payload, String signature);
}
