package com.shoestore.backend.service;

import com.shoestore.backend.dto.order.CreateOrderRequestDto;
import com.shoestore.backend.dto.order.OrderResponseDto;
import com.shoestore.backend.dto.order.UpdateOrderStatusRequestDto;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface OrderService {

    OrderResponseDto createOrder(CreateOrderRequestDto request, Authentication authentication);

    OrderResponseDto getOrderById(Long id, Authentication authentication);

    List<OrderResponseDto> getOrders(Authentication authentication);

    void updateOrderStatus(Long id, UpdateOrderStatusRequestDto request);
}
