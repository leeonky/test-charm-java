package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.util.ConvertException;

import java.lang.reflect.Parameter;

import static com.github.leeonky.util.NumberType.boxedClass;

public class CurryingArgument {
    private final Parameter parameter;
    protected final Data<?> data;
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

    public void dumpParameter(DumpingBuffer indentBuffer) {
        indentBuffer.newLine().dumpValue(data);
    }

    public static class Extraneous extends CurryingArgument {

        public Extraneous(Data<Object> data) {
            super(null, data);
        }

        @Override
        public boolean isConvertibleType() {
            return false;
        }

        @Override
        public boolean isSameType() {
            return false;
        }

        @Override
        public boolean isSuperType() {
            return false;
        }

        @Override
        public void dumpParameter(DumpingBuffer indentBuffer) {
            indentBuffer.newLine().append("*extraneous* ").dumpValue(data);
        }
    }
}
