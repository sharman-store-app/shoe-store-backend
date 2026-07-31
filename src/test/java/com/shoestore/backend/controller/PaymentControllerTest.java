package com.shoestore.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoestore.backend.dto.payment.CreatePaymentRequestDto;
import com.shoestore.backend.dto.payment.PaymentDto;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PaymentService paymentService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void createPaymentSessionReturnsSessionUrl() throws Exception {
        CreatePaymentRequestDto request = new CreatePaymentRequestDto(1L);
        when(paymentService.createPaymentSession(any(CreatePaymentRequestDto.class)))
                .thenReturn(new PaymentDto("https://checkout.stripe.test/session"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionUrl")
                        .value("https://checkout.stripe.test/session"));

        verify(paymentService).createPaymentSession(any(CreatePaymentRequestDto.class));
    }

    @Test
    void createPaymentSessionReturnsBadRequestForInvalidOrderId() throws Exception {
        CreatePaymentRequestDto request = new CreatePaymentRequestDto(0L);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void webhookDelegatesPayloadAndSignature() throws Exception {
        doNothing().when(paymentService).handleWebhook("{}", "signature");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "signature")
                        .content("{}"))
                .andExpect(status().isOk());

        verify(paymentService).handleWebhook("{}", "signature");
    }
}
