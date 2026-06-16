package com.shoestore.backend.dto.product.image;

import jakarta.validation.constraints.NotBlank;

public record CreateProductImageRequestDto(
        @NotBlank
        String color,
        @NotBlank
        String urlSmall,
        @NotBlank
        String urlMedium,
        @NotBlank
        String urlLarge,
        @NotBlank
        String urlOriginal
) {
}
