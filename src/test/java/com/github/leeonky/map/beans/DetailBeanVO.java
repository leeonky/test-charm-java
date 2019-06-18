package com.github.leeonky.map.beans;

import com.github.leeonky.map.Detail;
import com.github.leeonky.map.Mapping;

@Mapping(from = Bean.class, view = Detail.class)
public class DetailBeanVO {
    public String key, value;
}
