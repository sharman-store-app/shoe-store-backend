package com.shoestore.backend.repository;

import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.CartItem;
import com.shoestore.backend.model.ProductVariant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);

    List<CartItem> findByCartId(Long id);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(Long cartId);
}
