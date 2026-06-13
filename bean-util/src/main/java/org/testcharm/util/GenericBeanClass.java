package org.testcharm.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GenericBeanClass<T> extends BeanClass<T> {
    private final static Map<GenericType, GenericBeanClass<?>> instanceCache = new ConcurrentHashMap<>();
    private final GenericType genericType;

    @SuppressWarnings("unchecked")
    protected GenericBeanClass(GenericType genericType) {
        super((Class<T>) genericType.getRawType());
        this.genericType = genericType;
    }

    public static BeanClass<?> create(GenericType genericType) {
        return instanceCache.computeIfAbsent(genericType, GenericBeanClass::new);
    }

    @SuppressWarnings("unchecked")
    public static <T> GenericBeanClass<T> create(Class<?> rawClass, Type... types) {
        return (GenericBeanClass<T>) create(GenericType.createGenericType(new ParameterizedType() {

            @Override
            public Type[] getActualTypeArguments() {
                return types;
            }

            @Override
            public Type getRawType() {
                return rawClass;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        }));
    }

    @Override
    public Optional<BeanClass<?>> getTypeArguments(int position) {
        return genericType.getGenericTypeParameter(position).map(BeanClass::create);
    }

    @Override
    public boolean hasTypeArguments() {
        return genericType.hasTypeArguments();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GenericBeanClass.class, genericType);
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return Classes.equals(this, obj, GenericBeanClass.class, (self, another) ->
                Objects.equals(self.genericType, another.genericType));
    }

    @Override
    public Type getGenericType() {
        return genericType.getGenericType();
    }
}
