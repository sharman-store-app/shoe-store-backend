package com.shoestore.backend.mapper;

import com.shoestore.backend.config.MapperConfig;
import com.shoestore.backend.dto.cartitem.CartItemDto;
import com.shoestore.backend.model.CartItem;
import java.math.BigDecimal;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CartItemMapper {

    default CartItemDto toDto(CartItem cartItem, BigDecimal subtotal, String img) {
        return new CartItemDto(cartItem.getId(),
                cartItem.getProductVariant().getProduct().getName(),
                cartItem.getProductVariant().getProduct().getPrice(),
                cartItem.getProductVariant().getProduct().getPriceOld(),
                cartItem.getProductVariant().getColor(),
                cartItem.getProductVariant().getSize(),
                cartItem.getQuantity(), subtotal, img);
    }
}
