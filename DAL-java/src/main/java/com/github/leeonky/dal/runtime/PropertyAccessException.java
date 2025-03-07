package com.github.leeonky.dal.runtime;

public class PropertyAccessException extends java.lang.RuntimeException {
    private final String message;

    public PropertyAccessException(String message, Throwable cause) {
        super(cause);
        this.message = message;
    }

    public RuntimeException toDalError(String prefix, int position) {
        return new RuntimeException(prefix + message, position, getCause());
    }
}
