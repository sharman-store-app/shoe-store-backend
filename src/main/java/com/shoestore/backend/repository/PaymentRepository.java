package com.shoestore.backend.repository;

import com.shoestore.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderId(Long id);
}
