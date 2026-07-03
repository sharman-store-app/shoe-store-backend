package com.shoestore.backend.dto.order;

import java.math.BigDecimal;

public record OrderItemDto(
        Long id,
        String name,
        String imageUrl,
        String color,
        String size,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
