package com.shoestore.backend.service.impl;

import com.shoestore.backend.dto.cart.CartResponseDto;
import com.shoestore.backend.dto.cartitem.CartItemDto;
import com.shoestore.backend.dto.cartitem.CreateCartItemRequestDto;
import com.shoestore.backend.dto.cartitem.UpdateCartItemRequestDto;
import com.shoestore.backend.exceptation.InsufficientStockException;
import com.shoestore.backend.mapper.CartItemMapper;
import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.CartItem;
import com.shoestore.backend.model.ProductImage;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.CartItemRepository;
import com.shoestore.backend.repository.CartRepository;
import com.shoestore.backend.repository.ProductImageRepository;
import com.shoestore.backend.repository.ProductVariantRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.service.CartItemService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;

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
    public CartResponseDto changeQuantity(Long id, UpdateCartItemRequestDto request) {
        CartItem cartItem = findCartItem(id);
        cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        cartItem.setQuantity(request.quantity());
        if (cartItem.getQuantity() > cartItem.getProductVariant().getStockQty()) {
            throw new InsufficientStockException("Only "
                    + cartItem.getProductVariant().getStockQty()
                    + " items are available in stock.");
        }
        cartItemRepository.save(cartItem);
        return getResponse(cartItem.getCart().getId());
    }

    @Override
    @Transactional
    public CartResponseDto deleteCartItem(Long id) {
        CartItem cartItem = findCartItem(id);
        Long cartId = cartItem.getCart().getId();
        cartItemRepository.delete(cartItem);
        return getResponse(cartId);
    }

    private CartItem findCartItem(Long id) {
        return cartItemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Cart item with id " + id
                        + " not found in database"));
    }

    private CartResponseDto getResponse(Long cartId) {
        List<CartItem> cartItemList = cartItemRepository.findByCartId(cartId);
        List<CartItemDto> cartItemDtoList = new ArrayList<>();
        Integer productsCount = 0;
        BigDecimal cartSubtotal = BigDecimal.ZERO;
        for (CartItem item : cartItemList) {
            CartItemDto cartItemDto = cartItemMapper.toDto(item,
                    getSubtotal(item.getProductVariant(), item.getQuantity()), getImage(item));
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

    private String getImage(CartItem cartItem) {
        return productImageRepository.findByProductIdAndColor(
                        cartItem.getProductVariant().getProduct().getId(),
                        cartItem.getProductVariant().getColor())
                .map(ProductImage::getMainUrl)
                .orElse("");
    }
}
