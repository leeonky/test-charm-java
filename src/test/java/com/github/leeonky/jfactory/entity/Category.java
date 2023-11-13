package com.github.leeonky.jfactory.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class Category {
    private String id, name;
    private List<Product> products;
}
