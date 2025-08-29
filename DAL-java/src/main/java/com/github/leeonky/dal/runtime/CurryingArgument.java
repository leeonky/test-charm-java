package com.github.leeonky.dal.runtime;

import com.github.leeonky.util.ConvertException;

import java.lang.reflect.Parameter;

import static com.github.leeonky.util.NumberType.boxedClass;

public class CurryingArgument {
    private final Parameter parameter;
    private final Data<?> data;
    private Object properType;

    public CurryingArgument(Parameter parameter, Data<?> data) {
        this.parameter = parameter;
        this.data = data;
    }

    public boolean isSameType() {
        return data.value() != null && boxedClass(data.value().getClass()).equals(boxedClass(parameter.getType()));
    }

    public boolean isSuperType() {
        return data.value() != null && boxedClass(parameter.getType()).isInstance(data.value());
    }

    public boolean isConvertibleType() {
        try {
            properType();
            return true;
        } catch (ConvertException ignore) {
            return false;
        }
    }

    public Object properType() {
        if (properType == null)
            properType = data.convert(parameter.getType()).value();
        return properType;
    }

    public Data<?> origin() {
        return data;
    }

    @Override
    public String toString() {
        return data.dumpValue();
    }
}
