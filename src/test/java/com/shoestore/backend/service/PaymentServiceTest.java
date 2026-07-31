package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoestore.backend.dto.payment.CreatePaymentRequestDto;
import com.shoestore.backend.dto.payment.PaymentDto;
import com.shoestore.backend.exceptation.PaymentException;
import com.shoestore.backend.mapper.PaymentMapper;
import com.shoestore.backend.model.Order;
import com.shoestore.backend.model.OrderStatus;
import com.shoestore.backend.model.Payment;
import com.shoestore.backend.model.PaymentStatus;
import com.shoestore.backend.repository.OrderRepository;
import com.shoestore.backend.repository.PaymentRepository;
import com.shoestore.backend.service.impl.PaymentServiceImpl;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    private ObjectMapper objectMapper;
    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new PaymentServiceImpl(
                orderRepository,
                paymentRepository,
                paymentMapper,
                objectMapper
        );
        ReflectionTestUtils.setField(service, "frontendUrl", "https://shop.example");
        ReflectionTestUtils.setField(service, "stripeSecretKey", "sk_test");
        ReflectionTestUtils.setField(service, "stripeWebhookSecret", "whsec_test");
    }

    @Test
    void createPaymentSessionSavesPendingPayment() {
        Order order = new Order()
                .setId(10L)
                .setStatus(OrderStatus.PENDING)
                .setFinalAmount(new BigDecimal("12.34"));
        Session stripeSession = new Session();
        stripeSession.setId("cs_test");
        stripeSession.setUrl("https://stripe.example/session");
        PaymentDto dto = new PaymentDto("https://stripe.example/session");
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(10L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(dto);

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(stripeSession);

            PaymentDto response =
                    service.createPaymentSession(new CreatePaymentRequestDto(10L));

            assertEquals(dto, response);
            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(paymentCaptor.capture());
            assertEquals(PaymentStatus.PENDING, paymentCaptor.getValue().getPaymentStatus());
            assertEquals("cs_test", paymentCaptor.getValue().getSessionId());
            assertEquals(order, paymentCaptor.getValue().getOrder());
        }
    }

    @Test
    void createPaymentSessionThrowsWhenOrderAlreadyPaid() {
        Order order = new Order().setId(10L).setStatus(OrderStatus.PAID);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(PaymentException.class, () ->
                service.createPaymentSession(new CreatePaymentRequestDto(10L)));
    }

    @Test
    void createPaymentSessionThrowsWhenPaymentAlreadyExists() {
        Order order = new Order().setId(10L).setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(10L)).thenReturn(true);

        assertThrows(PaymentException.class, () ->
                service.createPaymentSession(new CreatePaymentRequestDto(10L)));
    }

    @Test
    void createPaymentSessionThrowsWhenStripeSecretMissing() {
        Order order = new Order().setId(10L).setStatus(OrderStatus.PENDING);
        ReflectionTestUtils.setField(service, "stripeSecretKey", " ");
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(10L)).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
                service.createPaymentSession(new CreatePaymentRequestDto(10L)));
    }

    @Test
    void handleWebhookMarksPaymentAndOrderPaid() {
        String payload = """
                {"data":{"object":{"id":"cs_test"}}}
                """;
        Event event = new Event();
        event.setType("checkout.session.completed");
        Order order = new Order().setStatus(OrderStatus.PENDING);
        Payment payment = new Payment()
                .setId(1L)
                .setPaymentStatus(PaymentStatus.PENDING)
                .setOrder(order);
        when(paymentRepository.findBySessionId("cs_test"))
                .thenReturn(Optional.of(payment));

        try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
            webhookStatic.when(() -> Webhook.constructEvent(
                    payload,
                    "sig",
                    "whsec_test"
            )).thenReturn(event);

            service.handleWebhook(payload, "sig");
        }

        assertEquals(PaymentStatus.PAID, payment.getPaymentStatus());
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void handleWebhookIgnoresNonCheckoutCompletedEvent() {
        Event event = new Event();
        event.setType("payment_intent.created");

        try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
            webhookStatic.when(() -> Webhook.constructEvent(
                    "{}",
                    "sig",
                    "whsec_test"
            )).thenReturn(event);

            service.handleWebhook("{}", "sig");
        }
    }

    @Test
    void handleWebhookThrowsWhenPaymentMissing() {
        String payload = """
                {"data":{"object":{"id":"cs_missing"}}}
                """;
        Event event = new Event();
        event.setType("checkout.session.completed");
        when(paymentRepository.findBySessionId("cs_missing")).thenReturn(Optional.empty());

        try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
            webhookStatic.when(() -> Webhook.constructEvent(
                    payload,
                    "sig",
                    "whsec_test"
            )).thenReturn(event);

            assertThrows(EntityNotFoundException.class, () ->
                    service.handleWebhook(payload, "sig"));
        }
    }
}
