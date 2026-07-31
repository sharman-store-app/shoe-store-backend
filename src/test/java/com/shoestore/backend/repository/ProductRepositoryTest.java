package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByNameReturnsProduct() {
        Product product = RepositoryTestData.product(entityManager, "Repository Runner");
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(productRepository.findByName("Repository Runner"))
                .isPresent()
                .get()
                .extracting(Product::getId)
                .isEqualTo(product.getId());
    }

    @Test
    void findByNameIgnoresSoftDeletedProduct() {
        Product product = RepositoryTestData.product(entityManager, "Deleted Runner");
        productRepository.delete(product);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(productRepository.findByName("Deleted Runner")).isEmpty();
    }
}
