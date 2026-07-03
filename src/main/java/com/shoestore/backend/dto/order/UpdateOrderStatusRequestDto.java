package com.shoestore.backend.dto.order;

import com.shoestore.backend.model.OrderStatus;
import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequestDto(
        @NotBlank
        OrderStatus status
) {
}
