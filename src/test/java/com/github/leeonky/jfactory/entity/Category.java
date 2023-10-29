package com.github.leeonky.jfactory.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Category {
    private long id;

    private String name;
}
