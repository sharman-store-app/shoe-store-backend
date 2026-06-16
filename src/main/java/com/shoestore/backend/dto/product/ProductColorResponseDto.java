package com.shoestore.backend.dto.product;

import com.shoestore.backend.dto.product.image.ProductImageResponseDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductColorResponseDto(
        Long id,
        String category,
        String name,
        String description,
        BigDecimal price,
        BigDecimal priceOld,
        String gender,
        String season,
        String material,
        List<String> sizes,
        Instant createdAt,
        List<ProductImageResponseDto> images
) {
}
