package com.shoestore.backend.service;

import com.shoestore.backend.dto.cart.CartResponseDto;
import com.shoestore.backend.dto.cartitem.CreateCartItemRequestDto;
import com.shoestore.backend.dto.cartitem.UpdateCartItemRequestDto;
import org.springframework.security.core.Authentication;

public interface CartItemService {

    CartResponseDto addItemToCart(CreateCartItemRequestDto request, Authentication authentication);

    CartResponseDto changeQuantity(Long id, UpdateCartItemRequestDto request);

    CartResponseDto deleteCartItem(Long id);
}
