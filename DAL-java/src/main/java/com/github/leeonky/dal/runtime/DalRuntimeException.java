package com.github.leeonky.dal.runtime;

@Deprecated
public class DalRuntimeException extends DalException {
    public DalRuntimeException(String message, int position) {
        this(message, position, null);
    }

    public DalRuntimeException(String message, int position, Throwable cause) {
        super(message, position, cause);
    }
}
