package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

abstract class MethodProperty implements Property {
    final Method method;

    MethodProperty(Method method) {
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
}
