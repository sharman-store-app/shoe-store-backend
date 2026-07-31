package com.shoestore.backend.dto.cartitem;

import java.math.BigDecimal;

public record CartItemDto(
        Long id,
        String name,
        BigDecimal price,
        BigDecimal priceOld,
        String color,
        String size,
        Integer quantity,
        BigDecimal subtotal,
        String imageUrl
) {
}
