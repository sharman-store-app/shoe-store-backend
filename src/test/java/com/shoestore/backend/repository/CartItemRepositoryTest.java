package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.CartItem;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.model.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByCartAndProductVariantReturnsCartItem() {
        Cart cart = cart();
        Product product = RepositoryTestData.product(entityManager, "Cart Item Runner");
        ProductVariant variant = RepositoryTestData.variant(
                entityManager,
                product,
                "Black",
                "42",
                "CART-ITEM-SKU-1"
        );
        CartItem cartItem = cartItem(cart, variant, 2);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(cartItemRepository.findByCartAndProductVariant(cart, variant))
                .isPresent()
                .get()
                .extracting(CartItem::getId)
                .isEqualTo(cartItem.getId());
        assertThat(cartItemRepository.findByCartId(cart.getId()))
                .extracting(CartItem::getId)
                .containsExactly(cartItem.getId());
    }

    @Test
    void deleteByCartIdRemovesCartItems() {
        Cart cart = cart();
        Product product = RepositoryTestData.product(entityManager, "Deleted Cart Item");
        ProductVariant variant = RepositoryTestData.variant(
                entityManager,
                product,
                "Black",
                "42",
                "CART-ITEM-SKU-2"
        );
        cartItem(cart, variant, 2);
        RepositoryTestData.flushAndClear(entityManager);

        cartItemRepository.deleteByCartId(cart.getId());
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(cartItemRepository.findByCartId(cart.getId())).isEmpty();
    }

    private Cart cart() {
        User user = RepositoryTestData.user(entityManager, "cart-item-user@example.com");
        return RepositoryTestData.cart(entityManager, user, LocalDateTime.now());
    }

    private CartItem cartItem(Cart cart, ProductVariant variant, int quantity) {
        CartItem cartItem = new CartItem()
                .setCart(cart)
                .setProductVariant(variant)
                .setQuantity(quantity);
        return entityManager.persistAndFlush(cartItem);
    }
}
