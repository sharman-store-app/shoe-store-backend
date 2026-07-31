package com.shoestore.backend.controller;

import com.shoestore.backend.dto.cart.CartMergeRequestDto;
import com.shoestore.backend.dto.cart.CartResponseDto;
import com.shoestore.backend.dto.cartitem.CreateCartItemRequestDto;
import com.shoestore.backend.dto.cartitem.UpdateCartItemRequestDto;
import com.shoestore.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "04. Cart and cart item", description = "Cart and cart item related endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    @GetMapping("/{cartId}")
    @Operation(summary = "Get cart",
            description = "Returns cart details including all cart items.")
    public CartResponseDto getCart(@PathVariable Long cartId) {
        return cartService.getCart(cartId);
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart",
            description = "Adds a product variant to a cart. Creates a new cart if necessary."
    )
    public CartResponseDto addItemToCart(@RequestBody @Valid CreateCartItemRequestDto request,
                                         Authentication authentication) {
        return cartService.addItemToCart(request, authentication);
    }

    @PatchMapping("/{cartId}/items/{cartItemId}")
    @Operation(summary = "Set new quantity", description = "Change item quantity in cart")
    public CartResponseDto changeQuantity(@PathVariable Long cartId, @PathVariable Long cartItemId,
                                          @RequestBody @Valid UpdateCartItemRequestDto request) {
        return cartService.changeQuantity(cartId, cartItemId, request);
    }

    @DeleteMapping("/{cartId}/items/{cartItemId}")
    @Operation(summary = "Delete cart item", description = "Removes an item from the cart")
    public CartResponseDto deleteCartItem(@PathVariable Long cartId,
                                          @PathVariable Long cartItemId) {
        return cartService.deleteCartItem(cartId, cartItemId);
    }

    @PostMapping("/merge")
    @Operation(summary = "Merge carts",
            description = "Merges a guest cart with the logged-in user's cart.")
    public CartResponseDto mergeCarts(@RequestBody @Valid CartMergeRequestDto
                                              request, Authentication authentication) {
        return cartService.mergeCarts(request, authentication);
    }
}
