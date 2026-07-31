package com.shoestore.backend.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartMergeRequestDto(
        @NotNull(message = EMPTY_MESSAGE) @Positive(message = POSITIVE_MESSAGE) Long cartId
) {
    private static final String EMPTY_MESSAGE = "Cart id can't be empty";
    private static final String POSITIVE_MESSAGE = "Cart id has to be greater than zero";
}
