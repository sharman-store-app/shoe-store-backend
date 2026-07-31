package com.shoestore.backend.dto.product.variant;

public record ProductVariantUpdateRequestDto(
        String size,
        String color,
        Integer stockQty,
        String sku
) {
}
