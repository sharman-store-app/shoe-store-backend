package com.shoestore.backend.controller;

import com.shoestore.backend.dto.payment.CreatePaymentRequestDto;
import com.shoestore.backend.dto.payment.PaymentDto;
import com.shoestore.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "07. Payment", description = "Payment endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create payment session",
            description = "Creates payment session for user")
    public PaymentDto createPaymentSession(
            @Valid @RequestBody CreatePaymentRequestDto request) {
        return paymentService.createPaymentSession(request);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook() {
        log.info("Stripe webhook received");
        return ResponseEntity.ok().build();
    }
}
