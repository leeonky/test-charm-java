package com.github.leeonky.jfactory;

public interface PropertyValue {

    static PropertyValue empty() {
        return new PropertyValue() {
            @Override
            public <T> Builder<T> applyToBuilder(String property, Builder<T> builder) {
                return builder;
            }
        };
    }

    <T> Builder<T> applyToBuilder(String property, Builder<T> builder);
}
