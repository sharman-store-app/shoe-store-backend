package com.shoestore.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLDelete(sql = "UPDATE product_variants SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "product_variants")
@Getter
@Setter
@Accessors(chain = true)
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String color;

    @Column(name = "stock_qty", nullable = false)
    private Integer stockQty;

    @Column(nullable = false, unique = true)
    private String sku;

    @OneToOne
    @JoinColumn(name = "product_image_id", nullable = false)
    private ProductImage productImage;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
