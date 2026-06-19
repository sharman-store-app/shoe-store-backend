package com.shoestore.backend.repository;

import com.shoestore.backend.model.ProductImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdIn(List<Long> productIds);

    List<ProductImage> findByProductId(Long productId);

    Optional<ProductImage> findByProductIdAndColor(Long productId, String color);

    boolean existsByColorAndMainUrl(String color, String mainUrl);

    Optional<ProductImage> findById(Long id);
}
