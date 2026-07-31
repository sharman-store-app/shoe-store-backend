package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.Order;
import com.shoestore.backend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByUserReturnsOrdersForUser() {
        User user = RepositoryTestData.user(entityManager, "order-user@example.com");
        Order order = RepositoryTestData.order(entityManager, user);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(orderRepository.findByUser(user))
                .extracting(Order::getId)
                .containsExactly(order.getId());
        assertThat(orderRepository.findById(order.getId()))
                .isPresent()
                .get()
                .extracting(Order::getId)
                .isEqualTo(order.getId());
    }
}
