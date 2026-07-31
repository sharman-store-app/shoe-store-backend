package com.shoestore.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@SQLDelete(sql = "UPDATE discounts SET is_deleted = true WHERE code=?")
@SQLRestriction("is_deleted = false")
@Table(name = "discount_codes")
@Getter
@Setter
@Accessors(chain = true)
public class Discount {
    @Id
    private String code;

    @Column(name = "discount_percentage", nullable = false)
    private BigDecimal discountPercentage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
