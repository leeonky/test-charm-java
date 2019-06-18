package com.github.leeonky.map.beans;

import com.github.leeonky.map.Detail;
import com.github.leeonky.map.Mapping;
import com.github.leeonky.map.MappingView;

import java.util.List;

@Mapping(from = Order.class, view = Detail.class)
public class DetailOrderVO {
    public String number;

    @MappingView(Detail.class)
    public List<DetailLineVO> lines;
}
