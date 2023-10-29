package com.github.leeonky.jfactory.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class Product {

    private long id;
    private String name;
    private String color;
    private String camelCaseName;

    private List<ProductStock> stocks = new ArrayList<>();

    private Category category;
}

