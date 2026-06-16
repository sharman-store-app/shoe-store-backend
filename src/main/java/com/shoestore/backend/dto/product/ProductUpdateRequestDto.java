package com.shoestore.backend.dto.product;

import java.math.BigDecimal;

public record ProductUpdateRequestDto(
        String category,
        String name,
        String description,
        BigDecimal price,
        BigDecimal priceOld,
        String gender,
        String season,
        String material
) {
}
