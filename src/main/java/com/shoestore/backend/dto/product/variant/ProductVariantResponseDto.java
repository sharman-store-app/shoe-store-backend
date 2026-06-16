package com.shoestore.backend.dto.product.variant;

public record ProductVariantResponseDto(
        String size,
        String color,
        Integer stockQty,
        String sku
) {
}
