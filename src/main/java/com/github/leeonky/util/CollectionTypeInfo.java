package com.github.leeonky.util;

class CollectionTypeInfo<T> extends ClassTypeInfo<T> {

    public CollectionTypeInfo(BeanClass<T> type) {
        super(type);
    }

    @Override
    public PropertyReader<T> getReader(String property) {
        try {
            Integer.valueOf(property);
            return new CollectionDataPropertyReader<>(type, property, type.getElementType());
        } catch (NumberFormatException ignore) {
            return super.getReader(property);
        }
    }

    @Override
    public PropertyWriter<T> getWriter(String property) {
        try {
            Integer.valueOf(property);
            return new CollectionDataPropertyWriter<>(type, property, type.getElementType());
        } catch (NumberFormatException ignore) {
            return super.getWriter(property);
        }
    }

    @Override
    public Property<T> getProperty(String name) {
        try {
            Integer.valueOf(name);
            return new DefaultProperty<>(name, type);
        } catch (NumberFormatException ignore) {
            return super.getProperty(name);
        }
    }
}
