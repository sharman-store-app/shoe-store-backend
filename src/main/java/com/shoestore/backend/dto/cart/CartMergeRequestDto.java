package com.shoestore.backend.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartMergeRequestDto(
        @NotNull(message = "Cart id can't be empty")
        @Positive(message = "Cart id has to be greater than zero") Long cartId
) {
}
