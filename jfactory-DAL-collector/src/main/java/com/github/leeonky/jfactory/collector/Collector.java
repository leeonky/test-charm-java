package com.github.leeonky.jfactory.collector;

import com.github.leeonky.jfactory.JFactory;

public class Collector extends UnitCollector {
    private final JFactory jFactory;
    private final Class<?> defaultType;

    public Collector(JFactory jFactory, Class<?> defaultType) {
        this.jFactory = jFactory;
        this.defaultType = defaultType;
    }

    public Object build() {
        return jFactory.type(defaultType).properties(propertiesMap()).create();
    }
}
