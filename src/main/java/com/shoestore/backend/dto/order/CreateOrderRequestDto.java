package com.shoestore.backend.dto.order;

import com.shoestore.backend.model.DeliveryType;
import com.shoestore.backend.model.PaymentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOrderRequestDto(
        @NotNull @Positive Long cartId,
        @Size(max = 50) String discountCode,
        @NotBlank(message = "First name can't be empty")
        String customerFirstName,
        @NotBlank(message = "Last name can't be empty")
        String customerLastName,
        @NotBlank(message = "Phone number can't be empty")
        String customerPhone,
        @NotBlank(message = "Email number can't be empty")
        @Email
        String customerEmail,
        @NotBlank(message = "Delivery address can't be empty")
        String deliveryAddress,
        @NotBlank(message = "Recipient name can't be empty")
        String recipientName,
        @NotBlank(message = "Recipient phone can't be empty")
        String recipientPhone,
        @NotNull(message = "Delivery type can't be empty") DeliveryType deliveryType,
        @NotNull(message = "Payment type can't be empty") PaymentType paymentType,
        boolean ignoreOutOfStockItems
) {
}
