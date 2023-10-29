package com.github.leeonky.jfactory.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ProductStock {

    private long id;

    private Product product;

    private String size;
    private int count;
}
