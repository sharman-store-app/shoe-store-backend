package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.Order;
import com.shoestore.backend.model.OrderItem;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.model.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByOrderIdReturnsOrderItems() {
        User user = RepositoryTestData.user(entityManager, "order-item-user@example.com");
        Order order = RepositoryTestData.order(entityManager, user);
        Product product = RepositoryTestData.product(entityManager, "Order Item Runner");
        ProductVariant variant = RepositoryTestData.variant(
                entityManager,
                product,
                "Black",
                "42",
                "ORDER-ITEM-SKU-1"
        );
        OrderItem item = new OrderItem()
                .setOrder(order)
                .setProductVariant(variant)
                .setQuantity(2)
                .setPriceAtOrder(BigDecimal.valueOf(99));
        entityManager.persistAndFlush(item);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(orderItemRepository.findByOrderId(order.getId()))
                .extracting(OrderItem::getId)
                .containsExactly(item.getId());
    }
}
