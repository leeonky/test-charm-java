package com.github.leeonky.dal.extensions.jdbc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "skus")
public class Sku {

    @Id
    private long id;

    private String name;

    @ManyToMany(mappedBy = "skus")
    private Set<Product> products = new HashSet<>();
}
