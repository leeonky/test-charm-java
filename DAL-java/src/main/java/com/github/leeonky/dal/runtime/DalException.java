package com.github.leeonky.dal.runtime;

import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.Sneaky;

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
        return buildMessage(this, super.getMessage());
    }

    @Override
    public String toString() {
        return getMessage();
    }

    public static String buildMessage(Throwable e, String message) {
        if (message != null && !message.isEmpty()) {
            Throwable cause = e.getCause();
            if (cause != null)
                return message + "\n" + cause;
            return message;
        }
        Throwable cause = e.getCause();
        if (cause != null)
            return cause.toString();
        return e.getClass().getName();
    }

    public static RuntimeException locateError(Throwable e, int positionBegin) {
        if (e instanceof InterpreterException || e instanceof ExpressionException)
            return (RuntimeException) e;
        if (e instanceof AssertionError)
            return new AssertionFailure(e.getMessage(), positionBegin);
        return new DalException(positionBegin, e);
    }

    public static Object throwUserRuntimeException(Throwable error) {
        return Sneaky.sneakyThrow(buildUserRuntimeException(error));
    }

    public static Throwable buildUserRuntimeException(Throwable error) {
        if (error instanceof DalRuntimeException
                || error instanceof UserRuntimeException
                || error instanceof AssertionError
                || error instanceof ExpressionException
                || error instanceof InterpreterException)
            return error;
        return new UserRuntimeException(error);
    }
}
