package com.shoestore.backend.dto.product;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateProductRequestDto(
        @NotBlank
        String name,
        @NotBlank
        String category,
        @NotBlank
        String description,
        @NotNull @Positive BigDecimal price,
        @Nullable @Positive BigDecimal priceOld,
        @NotBlank
        String gender,
        @NotBlank
        String season,
        @NotBlank
        String material
) {
}
