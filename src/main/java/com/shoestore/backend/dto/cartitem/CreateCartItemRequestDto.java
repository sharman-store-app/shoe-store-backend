package com.shoestore.backend.dto.cartitem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCartItemRequestDto(
        @Schema(description = "Guest cart identifier. Provide the existing cartId when adding "
                + "items to an existing guest cart. Leave null for the first item or for "
                + "authenticated users."
        ) Long cartId,
        @NotNull(message = "Product variant ID is required") Long productVariantId,
        @NotNull(message = QUANTITY_MESSAGE) @Positive(message = POSITIVE_MESSAGE) Integer quantity
) {
    private static final String QUANTITY_MESSAGE = "Quantity is required";
    private static final String POSITIVE_MESSAGE = "Quantity must be greater than zero";
}
