package com.github.leeonky.dal.runtime;

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
        String message = super.getMessage();
        if (message != null) {
            Throwable cause = getCause();
            if (cause != null)
                return message + "\n" + cause.getMessage();
            return message;
        }
        Throwable cause = getCause();
        if (cause != null)
            return cause.getMessage();

        return getClass().getName();
    }

    public DalException toDalError(int positionBegin) {
        return new DalException(positionBegin, this);
    }
}
