package com.shoestore.backend.dto.product.image;

public record ProductImageResponseDto(
        String color,
        String urlSmall,
        String urlMedium,
        String urlLarge,
        String urlOriginal
) {
}
