package com.github.leeonky.dal.runtime;

import com.github.leeonky.interpreter.InterpreterException;

import java.util.Optional;

public class DalException extends InterpreterException {
    private final Throwable cause;

    public DalException(String message, int position) {
        this(message, position, Position.Type.CHAR, null);
    }

    public DalException(String message, int position, Throwable cause) {
        this(message, position, Position.Type.CHAR, cause);
    }

    public DalException(String message, int position, Position.Type type) {
        this(message, position, type, null);
    }

    public DalException(String message, int position, Position.Type type, Throwable cause) {
        super(message, position, type);
        this.cause = cause;
    }

    public DalException(int position, Throwable cause) {
        this(null, position, Position.Type.CHAR, cause);
    }

    public DalException(int position, Position.Type type, Throwable cause) {
        this(null, position, type, cause);
    }

    public static Optional<Throwable> extractException(Throwable e) {
        if (e instanceof UserRuntimeException)
            return Optional.ofNullable(e.getCause());
        if (e.getCause() == null)
            return Optional.empty();
        return extractException(e.getCause());
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (message != null && !message.isEmpty()) {
            Throwable cause = getCause();
            if (cause != null)
                return message + "\n" + cause.getMessage();
            return message;
        }
        Throwable cause = getCause();
        if (cause != null)
            return cause.getMessage();

        return getClass().getName();

//        TODO merge message and cause message

//        String message = super.getMessage();
//        if (message != null) {
//            return message;
//        }
//        Throwable cause = getCause();
//        if (cause != null)
//            return cause.getMessage();
//        return getClass().getName();

    }

    public static Object handleException(Throwable error) {
        if (error instanceof DalRuntimeException)
            throw (DalRuntimeException) error;
        throw new UserRuntimeException(error);
    }
}
