package com.github.leeonky.map.beans;

import com.github.leeonky.map.MappingFrom;

@MappingFrom(ProductLine.class)
public class DetailProductLineVO extends DetailLineVO {
    public String product;
    public String productDetail;
}
