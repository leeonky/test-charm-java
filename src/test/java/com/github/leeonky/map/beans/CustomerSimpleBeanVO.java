package com.github.leeonky.map.beans;

import com.github.leeonky.map.MappingFrom;
import com.github.leeonky.map.MappingScope;
import com.github.leeonky.map.MappingView;
import com.github.leeonky.map.Simple;

@MappingFrom(Bean.class)
@MappingView(Simple.class)
@MappingScope(Customer.class)
public class CustomerSimpleBeanVO {
    public String key;
}
