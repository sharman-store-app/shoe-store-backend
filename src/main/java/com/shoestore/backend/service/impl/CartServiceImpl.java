package com.shoestore.backend.service.impl;

import com.shoestore.backend.dto.cart.CartMergeRequestDto;
import com.shoestore.backend.dto.cart.CartResponseDto;
import com.shoestore.backend.dto.cartitem.CartItemDto;
import com.shoestore.backend.dto.cartitem.CreateCartItemRequestDto;
import com.shoestore.backend.dto.cartitem.UpdateCartItemRequestDto;
import com.shoestore.backend.exceptation.InsufficientStockException;
import com.shoestore.backend.mapper.CartItemMapper;
import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.CartItem;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.CartItemRepository;
import com.shoestore.backend.repository.CartRepository;
import com.shoestore.backend.repository.ProductVariantRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public CartResponseDto getCart(Long id) {
        Cart cart = cartRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Cart with id " + id + " wasn't found in database"));
        return getResponse(cart.getId());
    }

    @Override
    @Transactional
    public CartResponseDto addItemToCart(CreateCartItemRequestDto request,
                                         Authentication authentication) {
        ProductVariant productVariant =
                productVariantRepository.findById(request.productVariantId()).orElseThrow(
                        () -> new EntityNotFoundException("Product variant with id "
                                + request.productVariantId() + " doesn't exist in database")
                );
        User user = authentication != null
                ? userRepository.findByEmail(authentication.getName()).orElse(null) : null;
        Cart cart;
        if (user == null && request.cartId() == null) {
            cart = cartRepository.save(new Cart());
        } else if (user == null && request.cartId() != null) {
            cart = cartRepository.findById(request.cartId()).orElseThrow(
                    () -> new EntityNotFoundException("Guest cart with id " + request.cartId()
                            + " wasn't found in database"));
        } else {
            cart = cartRepository.findByUser(user).orElseGet(() -> cartRepository.save(
                    new Cart().setUser(user)));
        }
        CartItem cartItem = cartItemRepository
                .findByCartAndProductVariant(cart, productVariant)
                .orElse(new CartItem().setCart(cart).setProductVariant(productVariant)
                        .setQuantity(0));
        cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        if (cartItem.getQuantity() > productVariant.getStockQty()) {
            throw new InsufficientStockException("Only " + productVariant.getStockQty()
                    + " items are available in stock.");
        }
        cartItemRepository.save(cartItem);
        return getResponse(cart.getId());
    }

    @Override
    @Transactional
    public CartResponseDto changeQuantity(Long cartId, Long cartItemId,
                                          UpdateCartItemRequestDto request) {
        CartItem cartItem = getCartItem(cartId, cartItemId);
        if (request.quantity() > cartItem.getProductVariant().getStockQty()) {
            throw new InsufficientStockException("Only "
                    + cartItem.getProductVariant().getStockQty()
                    + " items are available in stock.");
        }
        cartItem.setQuantity(request.quantity());
        return getResponse(cartId);
    }

    @Override
    @Transactional
    public CartResponseDto deleteCartItem(Long cartId, Long cartItemId) {
        CartItem cartItem = getCartItem(cartId, cartItemId);
        cartItemRepository.delete(cartItem);
        return getResponse(cartId);
    }

    @Override
    @Transactional
    public CartResponseDto mergeCarts(CartMergeRequestDto request, Authentication authentication) {
        Cart guestCart = cartRepository.findById(request.cartId()).orElseThrow(
                () -> new EntityNotFoundException("Cart with id " + request.cartId()
                        + " not found in database"));
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new EntityNotFoundException("User with email " + authentication.getName()
                        + " not found in database"));
        Optional<Cart> cartOptional = cartRepository.findByUser(user);
        if (cartOptional.isEmpty()) {
            guestCart.setUser(user);
            return getResponse(guestCart.getId());
        }
        Cart userCart = cartOptional.get();
        List<CartItem> guestCartItemList = cartItemRepository.findByCartId(guestCart.getId());
        List<CartItem> userCartItemList = cartItemRepository.findByCartId(userCart.getId());

        for (CartItem guestCartItem : guestCartItemList) {
            Optional<CartItem> userCartItemOptional = userCartItemList.stream()
                    .filter(userCartItem -> userCartItem.getProductVariant().getId()
                            .equals(guestCartItem.getProductVariant().getId()))
                    .findFirst();

            if (userCartItemOptional.isPresent()) {
                CartItem userCartItem = userCartItemOptional.get();
                Integer cartQuantity = userCartItem.getQuantity() + guestCartItem.getQuantity();

                if (userCartItem.getProductVariant().getStockQty() < cartQuantity) {
                    throw new InsufficientStockException("Only "
                            + userCartItem.getProductVariant().getStockQty()
                            + " items are available in stock.");
                }

                userCartItem.setQuantity(cartQuantity);
                cartItemRepository.delete(guestCartItem);
            } else {
                guestCartItem.setCart(userCart);
            }
        }
        cartRepository.delete(guestCart);
        return getResponse(userCart.getId());
    }

    private CartItem getCartItem(Long cartId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
                () -> new EntityNotFoundException("Cart item with id " + cartItemId
                        + " not found in database"));
        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new EntityNotFoundException("Cart item with id " + cartItemId
                    + " wasn't found in cart with id " + cartId);
        }
        return cartItem;
    }

    private CartResponseDto getResponse(Long cartId) {
        List<CartItem> cartItemList = cartItemRepository.findByCartId(cartId);
        List<CartItemDto> cartItemDtoList = new ArrayList<>();
        Integer productsCount = 0;
        BigDecimal cartSubtotal = BigDecimal.ZERO;
        for (CartItem item : cartItemList) {
            CartItemDto cartItemDto = cartItemMapper.toDto(item,
                    getSubtotal(item.getProductVariant(), item.getQuantity()));
            cartItemDtoList.add(cartItemDto);
            productsCount++;
            BigDecimal itemSubtotal = item.getProductVariant().getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            cartSubtotal = cartSubtotal.add(itemSubtotal);
        }
        return new CartResponseDto(cartId, productsCount, cartSubtotal, cartItemDtoList);
    }

    private BigDecimal getSubtotal(ProductVariant productVariant, Integer quantity) {
        return productVariant.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(quantity));
    }
}
