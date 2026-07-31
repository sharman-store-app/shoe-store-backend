package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shoestore.backend.dto.order.CreateOrderRequestDto;
import com.shoestore.backend.dto.order.OrderItemDto;
import com.shoestore.backend.dto.order.OrderResponseDto;
import com.shoestore.backend.dto.order.UpdateOrderStatusRequestDto;
import com.shoestore.backend.exceptation.EmptyCartException;
import com.shoestore.backend.exceptation.InvalidCustomerException;
import com.shoestore.backend.exceptation.OutOfStockException;
import com.shoestore.backend.mapper.OrderMapper;
import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.CartItem;
import com.shoestore.backend.model.DeliveryType;
import com.shoestore.backend.model.Discount;
import com.shoestore.backend.model.Order;
import com.shoestore.backend.model.OrderItem;
import com.shoestore.backend.model.OrderStatus;
import com.shoestore.backend.model.PaymentType;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductImage;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.CartItemRepository;
import com.shoestore.backend.repository.CartRepository;
import com.shoestore.backend.repository.DiscountRepository;
import com.shoestore.backend.repository.OrderItemRepository;
import com.shoestore.backend.repository.OrderRepository;
import com.shoestore.backend.repository.ProductImageRepository;
import com.shoestore.backend.repository.ProductVariantRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.service.impl.OrderServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private DiscountRepository discountRepository;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private OrderServiceImpl service;

    @Test
    void createOrderCreatesOrderAndReducesStock() {
        User user = new User().setId(1L).setEmail("user@example.com");
        Cart cart = new Cart().setId(10L).setUser(user);
        ProductVariant variant = variant(5L, 10);
        CartItem cartItem = cartItem(2L, cart, variant, 2);
        Discount discount = new Discount()
                .setCode("SAVE10")
                .setDiscountPercentage(BigDecimal.TEN);
        Order savedOrder = new Order().setId(30L);
        OrderItemDto itemDto = new OrderItemDto(
                40L,
                "Runner",
                "img",
                "Black",
                "42",
                new BigDecimal("100.00"),
                2,
                new BigDecimal("200.00")
        );
        OrderResponseDto dto = orderResponseDto(30L, List.of(itemDto));
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of(cartItem));
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(discountRepository.findById("SAVE10")).thenReturn(Optional.of(discount));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(productImageRepository.findByProductIdAndColor(1L, "Black"))
                .thenReturn(Optional.of(new ProductImage().setMainUrl("img")));
        when(orderMapper.toDto(any(OrderItem.class), eq("img")))
                .thenReturn(itemDto);
        when(orderMapper.toDto(savedOrder, List.of(itemDto))).thenReturn(dto);

        OrderResponseDto response = service.createOrder(orderRequest(false), authentication);

        assertEquals(dto, response);
        assertEquals(8, variant.getStockQty());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(new BigDecimal("200.00"), orderCaptor.getValue().getTotalAmount());
        assertEquals(new BigDecimal("20.00"), orderCaptor.getValue().getDiscountAmount());
        assertEquals(new BigDecimal("180.00"), orderCaptor.getValue().getFinalAmount());
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(productVariantRepository).save(variant);
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void createOrderThrowsWhenCartIsEmpty() {
        Cart cart = new Cart().setId(10L);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of());

        assertThrows(EmptyCartException.class, () ->
                service.createOrder(orderRequest(false), null));
    }

    @Test
    void createOrderThrowsWhenLoggedUserEmailDiffersFromRequest() {
        User user = new User().setId(1L).setEmail("other@example.com");
        Cart cart = new Cart().setId(10L).setUser(user);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem(2L, cart, variant(5L, 10), 1)));
        when(authentication.getName()).thenReturn("other@example.com");
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidCustomerException.class, () ->
                service.createOrder(orderRequest(false), authentication));
    }

    @Test
    void createOrderThrowsWhenItemOutOfStockAndNotIgnored() {
        Cart cart = new Cart().setId(10L);
        ProductVariant variant = variant(5L, 1);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem(2L, cart, variant, 2)));

        assertThrows(OutOfStockException.class, () ->
                service.createOrder(orderRequest(false), null));
    }

    @Test
    void getOrderByIdReturnsOrderForOwner() {
        User user = new User().setId(1L).setEmail("user@example.com");
        Order order = new Order().setId(30L).setUser(user);
        ProductVariant variant = variant(5L, 10);
        OrderItem orderItem = new OrderItem()
                .setId(40L)
                .setOrder(order)
                .setProductVariant(variant)
                .setQuantity(1)
                .setPriceAtOrder(new BigDecimal("100.00"));
        OrderItemDto itemDto = new OrderItemDto(
                40L,
                "Runner",
                null,
                "Black",
                "42",
                new BigDecimal("100.00"),
                1,
                new BigDecimal("100.00")
        );
        OrderResponseDto dto = orderResponseDto(30L, List.of(itemDto));
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(30L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(30L)).thenReturn(List.of(orderItem));
        when(productImageRepository.findByProductIdAndColor(1L, "Black"))
                .thenReturn(Optional.empty());
        when(orderMapper.toDto(orderItem, null)).thenReturn(itemDto);
        when(orderMapper.toDto(order, List.of(itemDto))).thenReturn(dto);

        OrderResponseDto response = service.getOrderById(30L, authentication);

        assertEquals(dto, response);
    }

    @Test
    void updateOrderStatusChangesStatus() {
        Order order = new Order().setId(30L).setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(30L)).thenReturn(Optional.of(order));

        service.updateOrderStatus(30L, new UpdateOrderStatusRequestDto(OrderStatus.SHIPPED));

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void updateOrderStatusThrowsWhenOrderMissing() {
        when(orderRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                service.updateOrderStatus(
                        30L,
                        new UpdateOrderStatusRequestDto(OrderStatus.SHIPPED)
                ));
    }

    private CreateOrderRequestDto orderRequest(boolean ignoreOutOfStockItems) {
        return new CreateOrderRequestDto(
                10L,
                " save10 ",
                "Ann",
                "Lee",
                "+48123456789",
                "user@example.com",
                "Street 1",
                "Ann Lee",
                "+48123456789",
                DeliveryType.COURIER,
                PaymentType.CARD,
                ignoreOutOfStockItems
        );
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

    private OrderResponseDto orderResponseDto(Long id, List<OrderItemDto> items) {
        return new OrderResponseDto(
                id,
                OrderStatus.PENDING,
                new BigDecimal("200.00"),
                new BigDecimal("20.00"),
                new BigDecimal("180.00"),
                "Ann",
                "Lee",
                "+48123456789",
                "user@example.com",
                "Street 1",
                "Ann Lee",
                "+48123456789",
                DeliveryType.COURIER,
                PaymentType.CARD,
                null,
                items
        );
    }
}
