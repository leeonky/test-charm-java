package com.github.leeonky.dal.runtime;

public class PropertyAccessException extends DALRuntimeException {
    public PropertyAccessException(Object property, Throwable cause) {
        super(buildMessage(property), cause);
    }

    private static String buildMessage(Object property) {
        return String.format("Get property `%s` failed, property can be:\n" +
                "  1. public field\n" +
                "  2. public getter\n" +
                "  3. public method\n" +
                "  4. Map key value\n" +
                "  5. customized type getter\n" +
                "  6. static method extension", property);
    }
}
