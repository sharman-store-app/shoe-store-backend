package com.shoestore.backend.service.impl;

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
import com.shoestore.backend.model.Discount;
import com.shoestore.backend.model.Order;
import com.shoestore.backend.model.OrderItem;
import com.shoestore.backend.model.OrderStatus;
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
import com.shoestore.backend.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final OrderMapper orderMapper;
    private final DiscountRepository discountRepository;

    @Override
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto request,
                                        Authentication authentication) {
        Cart cart = cartRepository.findById(request.cartId()).orElseThrow(
                () -> new EntityNotFoundException("Cart with id " + request.cartId()
                        + " doesn't' exist in database")
        );
        List<CartItem> cartItemList = cartItemRepository.findByCartId(cart.getId());

        if (cartItemList.isEmpty()) {
            throw new EmptyCartException("Cart with id " + cart.getId() + " is empty");
        }

        Order order = new Order();

        if (authentication != null) {
            User user = getUser(authentication);
            if (!request.customerEmail().equals(user.getEmail())) {
                throw new InvalidCustomerException("Entered email does not match the logged-in "
                        + "user's email.");
            }
            if (cart.getUser() != null && !user.getId().equals(cart.getUser().getId())) {
                throw new InvalidCustomerException("The cart does not belong to "
                        + "the logged-in user.");
            }
            order.setUser(user);
        } else {
            order.setUser(null);
        }
        order.setCustomerFirstName(request.customerFirstName())
                .setCustomerLastName(request.customerLastName())
                .setCustomerPhone(request.customerPhone())
                .setCustomerEmail(request.customerEmail())
                .setDeliveryAddress(request.deliveryAddress())
                .setRecipientName(request.recipientName())
                .setRecipientPhone(request.recipientPhone())
                .setDeliveryType(request.deliveryType())
                .setPaymentType(request.paymentType());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<CartItem> cartItemListToOrder = new ArrayList<>();
        for (CartItem item : cartItemList) {
            Integer variantQuantity = item.getQuantity();
            ProductVariant productVariant = item.getProductVariant();
            if (productVariant.getStockQty() < variantQuantity
                    && !request.ignoreOutOfStockItems()) {
                throw new OutOfStockException("Cart item with id " + item.getId()
                        + " is out of stock");
            }
            if (productVariant.getStockQty() >= variantQuantity) {
                BigDecimal itemAmount = productVariant.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(variantQuantity));
                totalAmount = totalAmount.add(itemAmount);
                cartItemListToOrder.add(item);
            }
        }

        if (cartItemListToOrder.isEmpty()) {
            throw new EmptyCartException("No items are available to create an order");
        }

        Discount discount = findDiscount(request.discountCode());
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discount != null) {
            discountAmount = totalAmount
                    .multiply(discount.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        order.setTotalAmount(totalAmount).setDiscount(discount).setDiscountAmount(discountAmount)
                .setFinalAmount(finalAmount).setStatus(OrderStatus.PENDING);

        order = orderRepository.save(order);

        List<OrderItemDto> orderItemDtoList = new ArrayList<>();

        for (CartItem item : cartItemListToOrder) {
            OrderItem orderItem = new OrderItem().setOrder(order)
                    .setProductVariant(item.getProductVariant())
                    .setQuantity(item.getQuantity())
                    .setPriceAtOrder(item.getProductVariant().getProduct().getPrice());
            ProductVariant productVariant = item.getProductVariant();
            productVariant.setStockQty(productVariant.getStockQty() - item.getQuantity());
            orderItemRepository.save(orderItem);
            productVariantRepository.save(productVariant);
            cartItemRepository.delete(item);
            String imgUrl = getProductImgUrl(item.getProductVariant());
            orderItemDtoList.add(orderMapper.toDto(orderItem, imgUrl));
        }
        return orderMapper.toDto(order, orderItemDtoList);
    }

    @Override
    public OrderResponseDto getOrderById(Long id, Authentication authentication) {
        User user = getUser(authentication);
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Order with id " + id
                        + " doesn't exist in database")
        );
        if (order.getUser() == null || !order.getUser().equals(user)) {
            throw new InvalidCustomerException("The order does not belong to the logged-in user.");
        }

        List<OrderItem> orderItemList = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDto> orderItemDtoList = new ArrayList<>();
        for (OrderItem item : orderItemList) {
            String imgUrl = getProductImgUrl(item.getProductVariant());
            orderItemDtoList.add(orderMapper.toDto(item, imgUrl));
        }

        return orderMapper.toDto(order, orderItemDtoList);
    }

    @Override
    public List<OrderResponseDto> getOrders(Authentication authentication) {
        User user = getUser(authentication);
        List<Order> orderList = orderRepository.findByUser(user);
        List<OrderResponseDto> orderResponseDtoList = new ArrayList<>();
        for (Order order : orderList) {
            List<OrderItemDto> orderItemDtoList = getOrderItemDtoList(order);
            orderResponseDtoList.add(orderMapper.toDto(order, orderItemDtoList));
        }
        return orderResponseDtoList;
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, UpdateOrderStatusRequestDto request) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Order with id " + id
                        + " doesn't exist in database")
        );
        order.setStatus(request.status());
    }

    private String getProductImgUrl(ProductVariant productVariant) {
        ProductImage productImage = productImageRepository
                .findByProductIdAndColor(productVariant.getId(),
                        productVariant.getColor()).orElse(null);
        if (productImage != null) {
            return productImage.getMainUrl();
        }
        return null;
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new EntityNotFoundException("User with email " + authentication.getName()
                        + " doesn't exist in database")
        );
    }

    private List<OrderItemDto> getOrderItemDtoList(Order order) {
        List<OrderItem> orderItemList = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDto> orderItemDtoList = new ArrayList<>();
        for (OrderItem item : orderItemList) {
            String imgUrl = getProductImgUrl(item.getProductVariant());
            orderItemDtoList.add(orderMapper.toDto(item, imgUrl));
        }
        return orderItemDtoList;
    }

    private Discount findDiscount(String discountCode) {
        Discount discount = null;
        if (discountCode != null && !discountCode.isBlank()) {
            String code = discountCode.trim().toUpperCase(Locale.ROOT);
            discount = discountRepository.findById(code)
                    .orElseThrow(() -> new EntityNotFoundException("Discount code "
                            + discountCode + " wasn't found in database"));
        }
        return discount;
    }
}
