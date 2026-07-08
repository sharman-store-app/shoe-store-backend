package com.shoestore.backend.service;

import com.shoestore.backend.dto.payment.CreatePaymentRequestDto;
import com.shoestore.backend.dto.payment.PaymentDto;
import com.shoestore.backend.dto.payment.PaymentResponseDto;

public interface PaymentService {

    PaymentDto createPaymentSession(CreatePaymentRequestDto request);

    PaymentResponseDto paymentSuccess(String sessionId);
}
