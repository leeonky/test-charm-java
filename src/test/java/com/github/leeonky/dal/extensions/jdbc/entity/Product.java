package com.github.leeonky.dal.extensions.jdbc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    private long id;
    private long pid;
    private String name;
    private int price;
    private Instant createdAt;

    @ManyToMany
    @JoinTable(name = "sku_products",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "sku_id"))
    private Set<Sku> skus = new HashSet<>();
}
