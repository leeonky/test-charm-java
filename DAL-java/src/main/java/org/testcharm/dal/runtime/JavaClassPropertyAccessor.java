package org.testcharm.dal.runtime;

import org.testcharm.util.NoSuchAccessorException;

import static java.lang.String.format;

public class JavaClassPropertyAccessor<T> implements PropertyAccessor<T> {
    public static final JavaClassPropertyAccessor<Object> INSTANCE = new JavaClassPropertyAccessor<>();

    @Override
    public Object getValue(T instance, Object property) {
        try {
            return PropertyAccessor.super.getValue(instance, property);
        } catch (NoSuchAccessorException ignore) {
            throw new InvalidPropertyException(format("Method or property `%s` does not exist in `%s`", property,
                    instance.getClass().getName()));
        }
    }
}
