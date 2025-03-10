package com.github.leeonky.dal.runtime;

import static com.github.leeonky.dal.runtime.DalException.buildMessage;

public class DalRuntimeException extends RuntimeException {
    public DalRuntimeException(String message) {
        this(message, null);
    }

    public DalRuntimeException() {
        this(null, null);
    }

    public DalRuntimeException(Throwable cause) {
        this(null, cause);
    }

    public DalRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return buildMessage(this, super.getMessage());
    }

    @Override
    public String toString() {
        return getMessage();
    }

    public DalException toDalError(int positionBegin) {
        return DalException.toDalError(this, positionBegin);
    }
}
