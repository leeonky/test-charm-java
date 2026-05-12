package org.testcharm.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;

public class ProxyPropertyWriter<T> extends AbstractPropertyAccessor<T> implements PropertyWriter<T> {
    private final PropertyReader<T> getter;

    public ProxyPropertyWriter(BeanClass<T> type, PropertyReader<T> getter) {
        super(type);
        this.getter = getter;
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
        return null;
    }

    @Override
    public boolean isBeanProperty() {
        return getter.isBeanProperty();
    }
}
