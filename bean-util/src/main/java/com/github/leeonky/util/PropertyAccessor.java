package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;

public interface PropertyAccessor<T> {

    String getName();

    Object tryConvert(Object value);

    BeanClass<T> getBeanType();

    Type getGenericType();

    BeanClass<?> getType();

    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    default <A extends Annotation> Optional<A> annotation(Class<A> annotationClass) {
        return Optional.ofNullable(getAnnotation(annotationClass));
    }

    boolean isBeanProperty();
}
