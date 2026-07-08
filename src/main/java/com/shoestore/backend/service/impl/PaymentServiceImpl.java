package com.shoestore.backend.service.impl;

import com.shoestore.backend.dto.payment.CreatePaymentRequestDto;
import com.shoestore.backend.dto.payment.PaymentDto;
import com.shoestore.backend.dto.payment.PaymentResponseDto;
import com.shoestore.backend.exceptation.PaymentException;
import com.shoestore.backend.mapper.PaymentMapper;
import com.shoestore.backend.model.Order;
import com.shoestore.backend.model.OrderStatus;
import com.shoestore.backend.model.Payment;
import com.shoestore.backend.model.PaymentStatus;
import com.shoestore.backend.repository.OrderRepository;
import com.shoestore.backend.repository.PaymentRepository;
import com.shoestore.backend.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final String CURRENCY = "eur";

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    @Override
    @Transactional
    public PaymentDto createPaymentSession(CreatePaymentRequestDto request) {
        Order order = orderRepository.findById(request.orderId()).orElseThrow(
                () -> new EntityNotFoundException("Order with id " + request.orderId()
                        + " doesn't exist in data base")
        );
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new PaymentException("Order status is "
                    + order.getStatus().toString().toLowerCase() + " and cannot paid for");
        }
        if (paymentRepository.existsByOrderId(request.orderId())) {
            throw new PaymentException("Payment with order id " + request.orderId()
                    + " already exists");
        }

        validateStripeSecretKey();
        long amountInCents = toCents(order.getTotalAmount());
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl
                        + "/payments/success-redirect?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl
                        + "/payments/cancel?session_id={CHECKOUT_SESSION_ID}")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(CURRENCY)
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem
                                                                .PriceData.ProductData.builder()
                                                                .setName("Order id: "
                                                                        + order.getId())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("orderId", String.valueOf(order.getId()))
                .build();

        try {
            Session session = Session.create(params);
            validateCreatedSession(session);

            Payment payment = new Payment()
                    .setPaymentStatus(PaymentStatus.PENDING)
                    .setOrder(order)
                    .setSessionUrl(session.getUrl())
                    .setSessionId(session.getId())
                    .setAmount(order.getTotalAmount());
            payment = paymentRepository.save(payment);
            return paymentMapper.toDto(payment);

        } catch (StripeException e) {
            throw new IllegalStateException("Stripe error while creating Checkout session: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponseDto paymentSuccess(String sessionId) {
        return null;
    }

    private void validateStripeSecretKey() {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException("Stripe secret key is not configured");
        }
    }

    private long toCents(BigDecimal amount) {
        return amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();
    }

    private void validateCreatedSession(Session session) {
        if (session == null) {
            throw new IllegalStateException(
                    "Stripe returned null Checkout Session."
            );
        }
        if (session.getId() == null || session.getId().isBlank()) {
            throw new IllegalStateException("Stripe Checkout Session was created without an id");
        }
        if (session.getUrl() == null || session.getUrl().isBlank()) {
            throw new IllegalStateException("Stripe Checkout Session was created without a url");
        }
    }
}
