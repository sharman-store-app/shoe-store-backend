package com.shoestore.backend.mapper;

import com.shoestore.backend.config.MapperConfig;
import com.shoestore.backend.dto.order.OrderItemDto;
import com.shoestore.backend.dto.order.OrderResponseDto;
import com.shoestore.backend.model.Order;
import com.shoestore.backend.model.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface OrderMapper {

    default OrderItemDto toDto(OrderItem orderItem, String imageUrl) {
        BigDecimal subtotal = orderItem.getPriceAtOrder()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        return new OrderItemDto(orderItem.getId(),
                orderItem.getProductVariant().getProduct().getName(), imageUrl,
                orderItem.getProductVariant().getColor(), orderItem.getProductVariant().getSize(),
                orderItem.getPriceAtOrder(), orderItem.getQuantity(), subtotal);
    }

    default OrderResponseDto toDto(Order order, List<OrderItemDto> orderItemDtoList) {
        return new OrderResponseDto(order.getId(), order.getStatus(), order.getTotalAmount(),
                order.getDiscountAmount(), order.getFinalAmount(),
                order.getCustomerFirstName(), order.getCustomerLastName(),
                order.getCustomerPhone(), order.getCustomerEmail(), order.getDeliveryAddress(),
                order.getRecipientName(), order.getRecipientPhone(), order.getDeliveryType(),
                order.getPaymentType(), order.getCreatedAt(), orderItemDtoList);
    }
}
