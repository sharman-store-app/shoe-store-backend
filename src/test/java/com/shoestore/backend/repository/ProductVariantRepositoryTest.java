package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductVariant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductVariantRepositoryTest {

    @Autowired
    private ProductVariantRepository productVariantRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void productVariantFindersReturnMatchingVariants() {
        Product product = RepositoryTestData.product(entityManager, "Variant Runner");
        ProductVariant variant = RepositoryTestData.variant(
                entityManager,
                product,
                "Black",
                "42",
                "REPO-SKU-1"
        );
        RepositoryTestData.variant(entityManager, product, "White", "43", "REPO-SKU-2");
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(productVariantRepository.findByProductId(product.getId()))
                .hasSize(2);
        assertThat(productVariantRepository.findByProductIdIn(List.of(product.getId())))
                .hasSize(2);
        assertThat(productVariantRepository.findByProductIdAndColor(product.getId(), "Black"))
                .extracting(ProductVariant::getSku)
                .containsExactly("REPO-SKU-1");
        assertThat(productVariantRepository.existsByProductIdAndColorAndSize(
                product.getId(),
                "Black",
                "42"
        )).isTrue();
        assertThat(productVariantRepository.findBySku("REPO-SKU-1"))
                .isPresent()
                .get()
                .extracting(ProductVariant::getId)
                .isEqualTo(variant.getId());
    }

    @Test
    void findBySkuIgnoresSoftDeletedVariant() {
        Product product = RepositoryTestData.product(entityManager, "Deleted Variant");
        ProductVariant variant = RepositoryTestData.variant(
                entityManager,
                product,
                "Black",
                "42",
                "REPO-SKU-DELETED"
        );
        productVariantRepository.delete(variant);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(productVariantRepository.findBySku("REPO-SKU-DELETED")).isEmpty();
    }
}
