package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

abstract class FieldProperty implements Property {
    final Field field;

    FieldProperty(Field field) {
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
