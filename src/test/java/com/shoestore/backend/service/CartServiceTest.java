package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shoestore.backend.dto.cart.CartMergeRequestDto;
import com.shoestore.backend.dto.cart.CartResponseDto;
import com.shoestore.backend.dto.cartitem.CartItemDto;
import com.shoestore.backend.dto.cartitem.CreateCartItemRequestDto;
import com.shoestore.backend.dto.cartitem.UpdateCartItemRequestDto;
import com.shoestore.backend.exceptation.InsufficientStockException;
import com.shoestore.backend.mapper.CartItemMapper;
import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.CartItem;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductImage;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.CartItemRepository;
import com.shoestore.backend.repository.CartRepository;
import com.shoestore.backend.repository.ProductImageRepository;
import com.shoestore.backend.repository.ProductVariantRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.service.impl.CartServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private CartServiceImpl service;

    @Test
    void addItemToCartCreatesGuestCartWhenNoCartIdOrAuthentication() {
        ProductVariant variant = variant(5L, 10);
        Cart cart = new Cart().setId(9L);
        CartItemDto itemDto = cartItemDto(1L);
        when(productVariantRepository.findById(5L)).thenReturn(Optional.of(variant));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.findByCartAndProductVariant(cart, variant))
                .thenReturn(Optional.empty());
        when(cartItemRepository.findByCartId(9L))
                .thenReturn(List.of(cartItem(1L, cart, variant, 2)));
        when(productImageRepository.findByProductIdAndColor(1L, "Black"))
                .thenReturn(Optional.of(new ProductImage().setMainUrl("img")));
        when(cartItemMapper.toDto(any(), any(), any())).thenReturn(itemDto);

        CartResponseDto response = service.addItemToCart(
                new CreateCartItemRequestDto(null, 5L, 2),
                null
        );

        assertEquals(9L, response.cartId());
        assertEquals(1, response.productsCount());
        assertEquals(new BigDecimal("200.00"), response.cartSubtotal());
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItemToCartThrowsWhenRequestedQuantityExceedsStock() {
        ProductVariant variant = variant(5L, 1);
        Cart cart = new Cart().setId(9L);
        when(productVariantRepository.findById(5L)).thenReturn(Optional.of(variant));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.findByCartAndProductVariant(cart, variant))
                .thenReturn(Optional.empty());

        assertThrows(InsufficientStockException.class, () -> service.addItemToCart(
                new CreateCartItemRequestDto(null, 5L, 2),
                null
        ));
    }

    @Test
    void changeQuantityUpdatesCartItemQuantity() {
        ProductVariant variant = variant(5L, 10);
        Cart cart = new Cart().setId(9L);
        CartItem item = cartItem(11L, cart, variant, 1);
        when(cartItemRepository.findById(11L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartId(9L)).thenReturn(List.of(item));
        when(productImageRepository.findByProductIdAndColor(1L, "Black"))
                .thenReturn(Optional.empty());
        when(cartItemMapper.toDto(any(), any(), any())).thenReturn(cartItemDto(11L));

        CartResponseDto response = service.changeQuantity(
                9L,
                11L,
                new UpdateCartItemRequestDto(3)
        );

        assertEquals(3, item.getQuantity());
        assertEquals(new BigDecimal("300.00"), response.cartSubtotal());
    }

    @Test
    void changeQuantityThrowsWhenCartItemBelongsToAnotherCart() {
        ProductVariant variant = variant(5L, 10);
        Cart otherCart = new Cart().setId(8L);
        when(cartItemRepository.findById(11L))
                .thenReturn(Optional.of(cartItem(11L, otherCart, variant, 1)));

        assertThrows(EntityNotFoundException.class, () -> service.changeQuantity(
                9L,
                11L,
                new UpdateCartItemRequestDto(3)
        ));
    }

    @Test
    void mergeCartsCombinesMatchingItemsAndDeletesGuestCart() {
        User user = new User().setId(1L).setEmail("user@example.com");
        Cart guestCart = new Cart().setId(10L);
        Cart userCart = new Cart().setId(20L).setUser(user);
        ProductVariant variant = variant(5L, 10);
        CartItem guestItem = cartItem(1L, guestCart, variant, 2);
        CartItem userItem = cartItem(2L, userCart, variant, 3);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(guestCart));
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(userCart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of(guestItem));
        when(cartItemRepository.findByCartId(20L)).thenReturn(List.of(userItem));
        when(productImageRepository.findByProductIdAndColor(1L, "Black"))
                .thenReturn(Optional.empty());
        when(cartItemMapper.toDto(any(), any(), any())).thenReturn(cartItemDto(2L));

        CartResponseDto response = service.mergeCarts(
                new CartMergeRequestDto(10L),
                authentication
        );

        assertEquals(20L, response.cartId());
        assertEquals(5, userItem.getQuantity());
        verify(cartItemRepository).delete(guestItem);
        verify(cartRepository).delete(guestCart);
        assertTrue(userCart.getLastActivityAt() != null);
    }

    private ProductVariant variant(Long id, int stockQty) {
        Product product = new Product()
                .setId(1L)
                .setName("Runner")
                .setPrice(new BigDecimal("100.00"));
        return new ProductVariant()
                .setId(id)
                .setProduct(product)
                .setColor("Black")
                .setSize("42")
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
                "Runner",
                new BigDecimal("100.00"),
                null,
                "Black",
                "42",
                1,
                new BigDecimal("100.00"),
                "img"
        );
    }
}
