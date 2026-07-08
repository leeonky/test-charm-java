package org.testcharm.dal.runtime;

import org.testcharm.dal.runtime.inspector.DumpingBuffer;
import org.testcharm.util.ConvertException;

import java.lang.reflect.Parameter;

import static org.testcharm.util.NumberType.boxedClass;

public class CurryingArgument {
    private final Parameter parameter;
    protected final Data<?> data;
    private Object properType;

    public CurryingArgument(Parameter parameter, Data<?> data) {
        this.parameter = parameter;
        this.data = data;
    }

    public boolean isSameType() {
        if (data.value() == null)
            return !parameter.getType().isPrimitive();
        return boxedClass(data.value().getClass()).equals(boxedClass(parameter.getType()));
    }

    public boolean isSuperType() {
        return boxedClass(parameter.getType()).isInstance(data.value());
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
        public void dumpParameter(DumpingBuffer indentBuffer) {
            indentBuffer.newLine().append("*extraneous* ").dumpValue(data);
        }
    }
}
