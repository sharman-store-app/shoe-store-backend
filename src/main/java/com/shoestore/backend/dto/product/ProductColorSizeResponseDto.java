package com.shoestore.backend.dto.product;

import com.shoestore.backend.dto.product.image.ProductImageResponseDto;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductColorSizeResponseDto(
        Long id,
        String category,
        String name,
        String description,
        BigDecimal price,
        BigDecimal priceOld,
        String gender,
        String season,
        String material,
        Integer stockQty,
        Instant createdAt,
        ProductImageResponseDto images
) {
}
