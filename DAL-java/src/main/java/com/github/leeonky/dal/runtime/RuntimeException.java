package com.github.leeonky.dal.runtime;

import java.util.Optional;

public class RuntimeException extends DalException {
    private final Throwable cause;

    public RuntimeException(String message, int position) {
        this(message, position, null);
    }

    public RuntimeException(String message, int position, Throwable cause) {
        super(message, position);
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    public static Optional<Throwable> extractException(Throwable e) {
        if (e instanceof UserRuntimeException)
            return Optional.ofNullable(e.getCause());
        if (e.getCause() == null)
            return Optional.empty();
        return extractException(e.getCause());
    }
}
