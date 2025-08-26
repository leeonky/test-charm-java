package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

abstract class FieldPropertyAccessor<T> extends AbstractPropertyAccessor<T> {
    private final Field field;

    FieldPropertyAccessor(BeanClass<T> beanClass, Field field) {
        super(beanClass);
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return AnnotationGetter.getInstance().getAnnotation(field, annotationClass);
    }

    @Override
    public Type getGenericType() {
        return field.getGenericType();
    }
}
