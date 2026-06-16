package com.shoestore.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLDelete(sql = "UPDATE product_images SET is_deleted = true where id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "product_images")
@Getter
@Setter
@Accessors(chain = true)
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String color;

    @Column(name = "url_small")
    private String urlSmall;

    @Column(name = "url_medium")
    private String urlMedium;

    @Column(name = "url_large")
    private String urlLarge;

    @Column(name = "url_original")
    private String urlOriginal;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
