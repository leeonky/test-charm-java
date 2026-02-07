package com.github.leeonky.jfactory.collector;

import com.github.leeonky.jfactory.Builder;
import com.github.leeonky.jfactory.JFactory;

public class Collector extends UnitCollector {
    private final JFactory jFactory;
    private final Class<?> defaultType;

    public Collector(JFactory jFactory, Class<?> defaultType) {
        this.jFactory = jFactory;
        this.defaultType = defaultType;
    }

    public Collector(JFactory jFactory, String... traitsSpec) {
        this(jFactory, Object.class);
        setTraitsSpec(traitsSpec);
    }

    public Object build() {
        if (traitsSpec() == null) {
            if (defaultType.equals(Object.class)) {
                return value();
            }
        }
        return builder().properties(propertiesMap()).create();
    }

    private Builder<?> builder() {
        String[] traitsSpec = traitsSpec();
        return traitsSpec != null ? jFactory.spec(traitsSpec) : jFactory.type(defaultType);
    }
}
