package com.github.leeonky.map.beans;

import com.github.leeonky.map.Mapping;
import com.github.leeonky.map.MappingView;
import com.github.leeonky.map.Simple;

import java.util.List;

@Mapping(from = Order.class, view = Simple.class)
public class OrderVO {
    public String number;

    @MappingView(Simple.class)
    public List<LineVO> lines;
}
