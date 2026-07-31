package com.shoestore.backend.dto.product.image;

import java.util.List;

public record ProductImageResponseDto(
        String color,
        String mainUrl,
        List<String> urls
) {
}
