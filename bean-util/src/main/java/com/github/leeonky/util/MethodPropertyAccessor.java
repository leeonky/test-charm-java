package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

abstract class MethodPropertyAccessor<T> extends AbstractPropertyAccessor<T> {
    private final Method method;

    public Method getMethod() {
        return method;
    }

    MethodPropertyAccessor(BeanClass<T> beanClass, Method method) {
        super(beanClass);
        this.method = method;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        A annotation = AnnotationGetter.getInstance().getAnnotation(method, annotationClass);
        if (annotation != null)
            return annotation;
        try {
            return AnnotationGetter.getInstance().getAnnotation(
                    method.getDeclaringClass().getDeclaredField(getName()), annotationClass);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    @Override
    public boolean isBeanProperty() {
        return !Modifier.isStatic(method.getModifiers());
    }
}
