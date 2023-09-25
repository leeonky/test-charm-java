package com.github.leeonky.dal.extensions.jdbc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    private long id;
    private String name;
    private int price;
}
