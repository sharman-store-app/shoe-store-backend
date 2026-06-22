package com.shoestore.backend.repository;

import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
