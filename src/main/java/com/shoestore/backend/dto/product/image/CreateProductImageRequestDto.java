package com.shoestore.backend.dto.product.image;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateProductImageRequestDto(
        @NotBlank
        String color,
        @NotBlank
        String mainUrl,
        List<String> urls
) {
}
