package org.testcharm.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;

public class ProxyPropertyWriter<T> extends AbstractPropertyAccessor<T> implements PropertyWriter<T> {
    private final PropertyReader<T> getter;
    private final PropertyWriter<T> originalSetter;

    public ProxyPropertyWriter(BeanClass<T> type, PropertyReader<T> getter, PropertyWriter<T> originalSetter) {
        super(type);
        this.getter = getter;
        this.originalSetter = originalSetter;
    }

    @Override
    public BiConsumer<T, Object> setter() {
        return (instance, value) -> ((BeanClassProxy.PropertyHolder) instance).__properties().put(getName(), value);
    }

    @Override
    public String getName() {
        return getter.getName();
    }

    @Override
    public Type getGenericType() {
        return getter.getGenericType();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return originalSetter.getAnnotation(annotationClass);
    }

    @Override
    public boolean isBeanProperty() {
        return getter.isBeanProperty();
    }
}
