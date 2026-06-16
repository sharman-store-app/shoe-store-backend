package com.shoestore.backend.repository;

import com.shoestore.backend.model.ProductVariant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdIn(List<Long> productIds);

    List<ProductVariant> findByProductId(Long productId);

    List<ProductVariant> findByProductIdAndColor(Long productId, String color);

    boolean existsByProductIdAndColorAndSize(Long productId, String color, String size);

    Optional<ProductVariant> findById(Long id);

    Optional<ProductVariant> findBySku(String sku);
}
