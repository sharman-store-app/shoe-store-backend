package com.shoestore.backend.controller;

import com.shoestore.backend.dto.order.CreateOrderRequestDto;
import com.shoestore.backend.dto.order.OrderResponseDto;
import com.shoestore.backend.dto.order.UpdateOrderStatusRequestDto;
import com.shoestore.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "05. Orders", description = "Order related endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place order", description = "User make order")
    public OrderResponseDto createOrder(@RequestBody @Valid CreateOrderRequestDto request,
                                        Authentication authentication) {
        return orderService.createOrder(request, authentication);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get order by id",
            description = "Get order details for the authenticated user")
    public OrderResponseDto getOrder(@PathVariable Long id, Authentication authentication) {
        return orderService.getOrderById(id, authentication);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get user orders",
            description = "Get all orders for the authenticated user")
    public List<OrderResponseDto> getOrders(Authentication authentication) {
        return orderService.getOrders(authentication);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status",
            description = "Update the status of the selected order only to ADMIN users")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateOrderStatus(@PathVariable Long id,
                                  @RequestBody @Valid UpdateOrderStatusRequestDto request) {
        orderService.updateOrderStatus(id,request);
    }
}
