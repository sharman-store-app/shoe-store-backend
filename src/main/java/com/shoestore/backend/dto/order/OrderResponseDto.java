package com.shoestore.backend.dto.order;

import com.shoestore.backend.model.DeliveryType;
import com.shoestore.backend.model.OrderStatus;
import com.shoestore.backend.model.PaymentType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponseDto(
        Long orderId,
        OrderStatus status,
        BigDecimal totalAmound,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        String customerFirstName,
        String customerLastName,
        String customerPhone,
        String customerEmail,
        String deliveryAddress,
        String recipientName,
        String recipientPhone,
        DeliveryType deliveryType,
        PaymentType paymentType,
        Instant createdAt,
        List<OrderItemDto> orderItems
) {
}
