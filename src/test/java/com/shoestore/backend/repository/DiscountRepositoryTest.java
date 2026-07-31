package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.Discount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DiscountRepositoryTest {

    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByIdReturnsDiscount() {
        Discount discount = RepositoryTestData.discount(entityManager, "REPO10");
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(discountRepository.findById("REPO10"))
                .isPresent()
                .get()
                .extracting(Discount::getCode)
                .isEqualTo(discount.getCode());
    }

    @Test
    void findByIdIgnoresSoftDeletedDiscount() {
        Discount discount = RepositoryTestData.discount(entityManager, "DELETED10");
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE discount_codes SET is_deleted = true WHERE code = ?")
                .setParameter(1, discount.getCode())
                .executeUpdate();
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(discountRepository.findById("DELETED10")).isEmpty();
    }
}
