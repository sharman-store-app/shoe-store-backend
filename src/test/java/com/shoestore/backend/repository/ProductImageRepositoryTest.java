package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductImage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductImageRepositoryTest {

    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void productImageFindersReturnMatchingImages() {
        Product product = RepositoryTestData.product(entityManager, "Image Runner");
        ProductImage image = RepositoryTestData.image(entityManager, product, "Black");
        RepositoryTestData.image(entityManager, product, "White");
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(productImageRepository.findByProductId(product.getId())).hasSize(2);
        assertThat(productImageRepository.findByProductIdIn(List.of(product.getId()))).hasSize(2);
        assertThat(productImageRepository.findByProductIdAndColor(product.getId(), "Black"))
                .isPresent()
                .get()
                .extracting(ProductImage::getId)
                .isEqualTo(image.getId());
        assertThat(productImageRepository.existsByProductIdAndColor(product.getId(), "Black"))
                .isTrue();
    }

    @Test
    void findByProductIdAndColorIgnoresSoftDeletedImage() {
        Product product = RepositoryTestData.product(entityManager, "Deleted Image");
        ProductImage image = RepositoryTestData.image(entityManager, product, "Black");
        productImageRepository.delete(image);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(productImageRepository.findByProductIdAndColor(product.getId(), "Black"))
                .isEmpty();
    }
}
