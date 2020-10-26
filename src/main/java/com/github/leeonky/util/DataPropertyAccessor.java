package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

class DataPropertyAccessor<T> extends AbstractPropertyAccessor<T> {
    private final String name;
    private final BeanClass<?> type;

    DataPropertyAccessor(BeanClass<T> beanClass, String name, BeanClass<?> type) {
        super(beanClass);
        this.name = name;
        this.type = type;
    }

    @Override
    protected Type provideGenericType() {
        return type.getType();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return null;
    }

    @Override
    public BeanClass<?> getType() {
        return type;
    }
}
