package com.shoestore.backend.service;

import com.shoestore.backend.dto.cart.CartMergeRequestDto;
import com.shoestore.backend.dto.cart.CartResponseDto;
import com.shoestore.backend.dto.cartitem.CreateCartItemRequestDto;
import com.shoestore.backend.dto.cartitem.UpdateCartItemRequestDto;
import org.springframework.security.core.Authentication;

public interface CartService {
    CartResponseDto getCart(Long id);

    CartResponseDto addItemToCart(CreateCartItemRequestDto request, Authentication authentication);

    CartResponseDto changeQuantity(Long cartId, Long cartItemId, UpdateCartItemRequestDto request);

    CartResponseDto deleteCartItem(Long cartId, Long cartItemId);

    CartResponseDto mergeCarts(CartMergeRequestDto request, Authentication authentication);
}
