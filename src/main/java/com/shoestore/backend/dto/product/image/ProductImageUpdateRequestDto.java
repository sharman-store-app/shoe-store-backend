package com.shoestore.backend.dto.product.image;

public record ProductImageUpdateRequestDto(
        Long productId,
        String color,
        String urlSmall,
        String urlMedium,
        String urlLarge,
        String urlOriginal
) {
}
