package com.github.leeonky.util;

public interface PropertyProxyFactory<T> {
    PropertyProxyFactory NO_PROXY = new PropertyProxyFactory() {
    };

    default PropertyWriter<T> proxyWriter(PropertyWriter<T> writer) {
        return writer;
    }

    default PropertyReader<T> proxyReader(PropertyReader<T> reader) {
        return reader;
    }

    default Property<T> proxyProperty(Property<T> property) {
        return property;
    }
}
