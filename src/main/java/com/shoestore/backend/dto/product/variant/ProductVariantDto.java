package com.shoestore.backend.dto.product.variant;

public record ProductVariantDto(
        Long id,
        Long productId,
        String size,
        String color,
        Integer stockQty,
        String sku
) {
}
