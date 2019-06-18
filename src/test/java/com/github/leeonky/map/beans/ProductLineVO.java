package com.github.leeonky.map.beans;

import com.github.leeonky.map.MappingFrom;

@MappingFrom(ProductLine.class)
public class ProductLineVO extends LineVO {
    public String product;
}
