package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AnnotationGetter {

    private static AnnotationGetter annotationGetter = new AnnotationGetter();

    public static AnnotationGetter getInstance() {
        return annotationGetter;
    }

    public static void setAnnotationGetter(AnnotationGetter getter) {
        annotationGetter = getter;
    }

    public <A extends Annotation> A getAnnotation(Field field, Class<A> annotationClass) {
        return field.getAnnotation(annotationClass);
    }


    public <A extends Annotation> A getAnnotation(Method method, Class<A> annotationClass) {
        return method.getAnnotation(annotationClass);
    }
}
