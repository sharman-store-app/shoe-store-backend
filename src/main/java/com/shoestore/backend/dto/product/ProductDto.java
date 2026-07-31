package com.shoestore.backend.dto.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductDto(
        Long id,
        String category,
        String name,
        String description,
        BigDecimal price,
        BigDecimal priceOld,
        String gender,
        String season,
        String material,
        Instant createdAt
) {
}
