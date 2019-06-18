package com.github.leeonky.map.beans;

import com.github.leeonky.map.MappingFrom;

@MappingFrom(ServiceLine.class)
public class DetailServiceLineVO extends DetailLineVO {
    public String service;
    public String serviceDetail;
}
