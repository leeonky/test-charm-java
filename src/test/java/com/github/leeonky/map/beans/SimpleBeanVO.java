package com.github.leeonky.map.beans;

import com.github.leeonky.map.Mapping;
import com.github.leeonky.map.Simple;

@Mapping(from = Bean.class, view = Simple.class)
public class SimpleBeanVO {
    public String key;
}
