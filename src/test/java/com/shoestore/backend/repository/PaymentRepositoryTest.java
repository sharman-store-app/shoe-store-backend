package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.Order;
import com.shoestore.backend.model.Payment;
import com.shoestore.backend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void paymentFindersReturnMatchingPayment() {
        User user = RepositoryTestData.user(entityManager, "payment-user@example.com");
        Order order = RepositoryTestData.order(entityManager, user);
        Payment payment = RepositoryTestData.payment(entityManager, order, "session-123");
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(paymentRepository.existsByOrderId(order.getId())).isTrue();
        assertThat(paymentRepository.findBySessionId("session-123"))
                .isPresent()
                .get()
                .extracting(Payment::getId)
                .isEqualTo(payment.getId());
    }

    @Test
    void findBySessionIdIgnoresSoftDeletedPayment() {
        User user = RepositoryTestData.user(
                entityManager,
                "deleted-payment-user@example.com"
        );
        Order order = RepositoryTestData.order(entityManager, user);
        Payment payment = RepositoryTestData.payment(
                entityManager,
                order,
                "session-deleted"
        );
        paymentRepository.delete(payment);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(paymentRepository.findBySessionId("session-deleted")).isEmpty();
        assertThat(paymentRepository.existsByOrderId(order.getId())).isFalse();
    }
}
