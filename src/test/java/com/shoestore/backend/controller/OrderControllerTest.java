package com.shoestore.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoestore.backend.dto.order.CreateOrderRequestDto;
import com.shoestore.backend.dto.order.OrderItemDto;
import com.shoestore.backend.dto.order.OrderResponseDto;
import com.shoestore.backend.dto.order.UpdateOrderStatusRequestDto;
import com.shoestore.backend.model.DeliveryType;
import com.shoestore.backend.model.OrderStatus;
import com.shoestore.backend.model.PaymentType;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.OrderService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void createOrderReturnsOrder() throws Exception {
        CreateOrderRequestDto request = createOrderRequest();
        when(orderService.createOrder(any(CreateOrderRequestDto.class),
                nullable(Authentication.class))).thenReturn(orderResponse());

        mockMvc.perform(post("/api/orders")
                        .with(user("john@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).createOrder(any(CreateOrderRequestDto.class),
                nullable(Authentication.class));
    }

    @Test
    void createOrderReturnsBadRequestForInvalidEmail() throws Exception {
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                1L,
                null,
                "John",
                "Doe",
                "+48123456789",
                "bad-email",
                "Street 1",
                "John Doe",
                "+48123456789",
                DeliveryType.COURIER,
                PaymentType.CARD,
                false
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderReturnsOrder() throws Exception {
        when(orderService.getOrderById(eq(1L), nullable(Authentication.class)))
                .thenReturn(orderResponse());

        mockMvc.perform(get("/api/orders/{id}", 1L)
                        .with(user("john@example.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L));

        verify(orderService).getOrderById(eq(1L), nullable(Authentication.class));
    }

    @Test
    void getOrdersReturnsUserOrders() throws Exception {
        when(orderService.getOrders(nullable(Authentication.class)))
                .thenReturn(List.of(orderResponse()));

        mockMvc.perform(get("/api/orders")
                        .with(user("john@example.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(1L));

        verify(orderService).getOrders(nullable(Authentication.class));
    }

    @Test
    void updateOrderStatusReturnsNoContent() throws Exception {
        UpdateOrderStatusRequestDto request =
                new UpdateOrderStatusRequestDto(OrderStatus.SHIPPED);
        doNothing().when(orderService)
                .updateOrderStatus(eq(1L), any(UpdateOrderStatusRequestDto.class));

        mockMvc.perform(patch("/api/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(orderService).updateOrderStatus(eq(1L), any(UpdateOrderStatusRequestDto.class));
    }

    @Test
    void updateOrderStatusReturnsBadRequestForMissingStatus() throws Exception {
        mockMvc.perform(patch("/api/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private CreateOrderRequestDto createOrderRequest() {
        return new CreateOrderRequestDto(
                1L,
                null,
                "John",
                "Doe",
                "+48123456789",
                "john@example.com",
                "Street 1",
                "John Doe",
                "+48123456789",
                DeliveryType.COURIER,
                PaymentType.CARD,
                false
        );
    }

    private OrderResponseDto orderResponse() {
        OrderItemDto item = new OrderItemDto(
                2L,
                "Runner",
                "main.jpg",
                "Black",
                "42",
                BigDecimal.valueOf(99),
                2,
                BigDecimal.valueOf(198)
        );
        return new OrderResponseDto(
                1L,
                OrderStatus.PENDING,
                BigDecimal.valueOf(198),
                BigDecimal.ZERO,
                BigDecimal.valueOf(198),
                "John",
                "Doe",
                "+48123456789",
                "john@example.com",
                "Street 1",
                "John Doe",
                "+48123456789",
                DeliveryType.COURIER,
                PaymentType.CARD,
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(item)
        );
    }
}
