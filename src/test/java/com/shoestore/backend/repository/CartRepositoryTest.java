package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByUserReturnsUsersCart() {
        User user = RepositoryTestData.user(entityManager, "cart-user@example.com");
        Cart cart = RepositoryTestData.cart(
                entityManager,
                user,
                LocalDateTime.now()
        );
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(cartRepository.findByUser(user))
                .isPresent()
                .get()
                .extracting(Cart::getId)
                .isEqualTo(cart.getId());
    }

    @Test
    void findByLastActivityAtBeforeReturnsExpiredCarts() {
        User oldUser = RepositoryTestData.user(entityManager, "old-cart@example.com");
        User newUser = RepositoryTestData.user(entityManager, "new-cart@example.com");
        Cart oldCart = RepositoryTestData.cart(
                entityManager,
                oldUser,
                LocalDateTime.now().minusDays(10)
        );
        RepositoryTestData.cart(entityManager, newUser, LocalDateTime.now());
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(cartRepository.findByLastActivityAtBefore(LocalDateTime.now().minusDays(1)))
                .extracting(Cart::getId)
                .contains(oldCart.getId());
    }
}
