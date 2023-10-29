package com.github.leeonky.jfactory.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Order {

    private long id;
    private String customer;

    private Product product;
}
