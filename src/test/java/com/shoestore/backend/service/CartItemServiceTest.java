package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shoestore.backend.dto.cart.CartResponseDto;
import com.shoestore.backend.dto.cartitem.CartItemDto;
import com.shoestore.backend.dto.cartitem.CreateCartItemRequestDto;
import com.shoestore.backend.dto.cartitem.UpdateCartItemRequestDto;
import com.shoestore.backend.exceptation.InsufficientStockException;
import com.shoestore.backend.mapper.CartItemMapper;
import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.CartItem;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.repository.CartItemRepository;
import com.shoestore.backend.repository.CartRepository;
import com.shoestore.backend.repository.ProductImageRepository;
import com.shoestore.backend.repository.ProductVariantRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.service.impl.CartItemServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CartItemServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CartItemMapper cartItemMapper;
    @InjectMocks
    private CartItemServiceImpl service;

    @Test
    void addItemToCartAddsToExistingGuestCart() {
        ProductVariant variant = variant(4L, 5);
        Cart cart = new Cart().setId(2L);
        CartItem item = cartItem(6L, cart, variant, 1);
        when(productVariantRepository.findById(4L)).thenReturn(Optional.of(variant));
        when(cartRepository.findById(2L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProductVariant(cart, variant))
                .thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartId(2L)).thenReturn(List.of(item));
        when(productImageRepository.findByProductIdAndColor(1L, "White"))
                .thenReturn(Optional.empty());
        when(cartItemMapper.toDto(any(), any(), any())).thenReturn(cartItemDto(6L));

        CartResponseDto response = service.addItemToCart(
                new CreateCartItemRequestDto(2L, 4L, 2),
                null
        );

        assertEquals(3, item.getQuantity());
        assertEquals(new BigDecimal("150.00"), response.cartSubtotal());
        verify(cartItemRepository).save(item);
    }

    @Test
    void addItemToCartThrowsWhenVariantMissing() {
        when(productVariantRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.addItemToCart(
                new CreateCartItemRequestDto(null, 4L, 1),
                null
        ));
    }

    @Test
    void changeQuantitySavesNewQuantity() {
        ProductVariant variant = variant(4L, 5);
        Cart cart = new Cart().setId(2L);
        CartItem item = cartItem(6L, cart, variant, 1);
        when(cartItemRepository.findById(6L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartId(2L)).thenReturn(List.of(item));
        when(productImageRepository.findByProductIdAndColor(1L, "White"))
                .thenReturn(Optional.empty());
        when(cartItemMapper.toDto(any(), any(), any())).thenReturn(cartItemDto(6L));

        CartResponseDto response = service.changeQuantity(6L, new UpdateCartItemRequestDto(4));

        assertEquals(4, item.getQuantity());
        assertEquals(new BigDecimal("200.00"), response.cartSubtotal());
        verify(cartItemRepository).save(item);
    }

    @Test
    void changeQuantityThrowsWhenStockIsInsufficient() {
        ProductVariant variant = variant(4L, 2);
        Cart cart = new Cart().setId(2L);
        when(cartItemRepository.findById(6L))
                .thenReturn(Optional.of(cartItem(6L, cart, variant, 1)));

        assertThrows(InsufficientStockException.class, () ->
                service.changeQuantity(6L, new UpdateCartItemRequestDto(3)));
    }

    @Test
    void deleteCartItemRemovesItemAndReturnsResponse() {
        ProductVariant variant = variant(4L, 5);
        Cart cart = new Cart().setId(2L);
        CartItem item = cartItem(6L, cart, variant, 1);
        when(cartItemRepository.findById(6L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartId(2L)).thenReturn(List.of());

        CartResponseDto response = service.deleteCartItem(6L);

        assertEquals(2L, response.cartId());
        assertEquals(0, response.productsCount());
        verify(cartItemRepository).delete(item);
    }

    private ProductVariant variant(Long id, int stockQty) {
        Product product = new Product()
                .setId(1L)
                .setName("Trainer")
                .setPrice(new BigDecimal("50.00"));
        return new ProductVariant()
                .setId(id)
                .setProduct(product)
                .setColor("White")
                .setSize("40")
                .setStockQty(stockQty);
    }

    private CartItem cartItem(Long id, Cart cart, ProductVariant variant, int quantity) {
        return new CartItem()
                .setId(id)
                .setCart(cart)
                .setProductVariant(variant)
                .setQuantity(quantity);
    }

    private CartItemDto cartItemDto(Long id) {
        return new CartItemDto(
                id,
                "Trainer",
                new BigDecimal("50.00"),
                null,
                "White",
                "40",
                1,
                new BigDecimal("50.00"),
                ""
        );
    }
}
