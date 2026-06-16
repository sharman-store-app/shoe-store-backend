package com.shoestore.backend.dto.product.variant;

public record ProductVariantUpdateRequestDto(
        Long productId,
        String size,
        String color,
        Integer stockQty,
        String sku
) {
}
