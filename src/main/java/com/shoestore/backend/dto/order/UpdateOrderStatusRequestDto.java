package com.shoestore.backend.dto.order;

import com.shoestore.backend.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequestDto(
        @NotNull OrderStatus status
) {
}
