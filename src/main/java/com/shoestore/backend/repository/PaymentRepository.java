package com.shoestore.backend.repository;

import com.shoestore.backend.model.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderId(Long id);

    Optional<Payment> findBySessionId(String id);
}
