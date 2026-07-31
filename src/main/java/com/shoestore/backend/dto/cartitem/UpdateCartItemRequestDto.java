package com.shoestore.backend.dto.cartitem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequestDto(
        @Positive(message = POSITIVE_MESSAGE) @NotNull(message = EMPTY_MESSAGE) Integer quantity
) {
    private static final String POSITIVE_MESSAGE = "Quantity must be greater than zero";
    private static final String EMPTY_MESSAGE = "Cart id can't be empty";
}
