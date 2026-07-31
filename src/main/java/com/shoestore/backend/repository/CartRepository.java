package com.shoestore.backend.repository;

import com.shoestore.backend.model.Cart;
import com.shoestore.backend.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);

    List<Cart> findByLastActivityAtBefore(LocalDateTime dateTime);
}
