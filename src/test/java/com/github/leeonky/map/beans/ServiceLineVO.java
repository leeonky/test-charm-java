package com.github.leeonky.map.beans;

import com.github.leeonky.map.MappingFrom;

@MappingFrom(ServiceLine.class)
public class ServiceLineVO extends LineVO {
    public String service;
}
