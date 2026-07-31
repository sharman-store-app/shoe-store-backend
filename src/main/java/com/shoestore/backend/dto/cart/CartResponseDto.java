package com.shoestore.backend.dto.cart;

import com.shoestore.backend.dto.cartitem.CartItemDto;
import java.math.BigDecimal;
import java.util.List;

public record CartResponseDto(
        Long cartId,
        Integer productsCount,
        BigDecimal cartSubtotal,
        List<CartItemDto> cartItems
) {
}
