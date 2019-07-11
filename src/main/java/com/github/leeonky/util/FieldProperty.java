package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

abstract class FieldProperty<T> extends AbstractProperty<T> {
    final Field field;

    FieldProperty(BeanClass<T> beanClass, Field field) {
        super(beanClass);
        this.field = field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Class<?> getPropertyType() {
        return field.getType();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return AnnotationGetter.getInstance().getAnnotation(field, annotationClass);
    }

}
