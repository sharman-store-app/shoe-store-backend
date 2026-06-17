package com.shoestore.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "main_url", nullable = false)
    private String mainUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "urls")
    private List<String> urls;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
