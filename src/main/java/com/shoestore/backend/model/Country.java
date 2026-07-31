package com.shoestore.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "countries")
@Getter
public class Country {
    @Id
    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;
}
