package com.shoestore.backend.dto.product.image;

public record ProductImageDto(
        Long id,
        Long productId,
        String color,
        String urlSmall,
        String urlMedium,
        String urlLarge,
        String urlOriginal
) {
}
