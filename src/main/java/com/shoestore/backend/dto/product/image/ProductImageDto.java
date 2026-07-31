package com.shoestore.backend.dto.product.image;

import java.util.List;

public record ProductImageDto(
        Long id,
        Long productId,
        String color,
        String mainUrl,
        List<String> urls
) {
}
