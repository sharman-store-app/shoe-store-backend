package com.shoestore.backend.dto.cartitem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequestDto(
        @Positive(message = "Quantity must be greater than zero") @NotNull(message = "Quantity can't be empty") Integer quantity
) {
}
