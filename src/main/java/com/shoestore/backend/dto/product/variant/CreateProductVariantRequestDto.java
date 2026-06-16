package com.shoestore.backend.dto.product.variant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateProductVariantRequestDto(
        @NotBlank
        String size,
        @NotBlank
        String color,
        @Positive @NotNull Integer stockQty,
        String sku
) {
}
