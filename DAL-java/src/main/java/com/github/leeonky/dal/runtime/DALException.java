package com.github.leeonky.dal.runtime;

import com.github.leeonky.interpreter.InterpreterException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class DALException extends InterpreterException {
    private final Throwable cause;

    public DALException(String message, int position) {
        this(message, position, Position.Type.CHAR, null);
    }

    public DALException(String message, int position, Throwable cause) {
        this(message, position, Position.Type.CHAR, cause);
    }

    public DALException(String message, int position, Position.Type type) {
        this(message, position, type, null);
    }

    public DALException(String message, int position, Position.Type type, Throwable cause) {
        super(message, position, type);
        this.cause = cause;
    }

    public DALException(int position, Throwable cause) {
        this(null, position, Position.Type.CHAR, cause);
    }

    public DALException(int position, Position.Type type, Throwable cause) {
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
                return message + "\n" + clauseInfo(cause);
            return message;
        }
        Throwable cause = e.getCause();
        if (cause != null)
            return clauseInfo(cause);
        return e.getClass().getName();
    }

    private static String clauseInfo(Throwable cause) {
        String info = cause.toString();
        if (cause instanceof UserRuntimeException) {
            StringWriter writer = new StringWriter();
            cause.getCause().printStackTrace(new PrintWriter(writer));
            return writer.toString();
        }
        return info;
    }

    public static RuntimeException locateError(Throwable e, int positionBegin) {
        if (e instanceof InterpreterException || e instanceof ExpressionException)
            return (RuntimeException) e;
        if (e instanceof AssertionError)
            return new AssertionFailure(e.getMessage(), positionBegin);
        return new DALException(positionBegin, e);
    }

    public static Throwable buildUserRuntimeException(Throwable error) {
        if (error instanceof DALRuntimeException
                || error instanceof UserRuntimeException
                || error instanceof AssertionError
                || error instanceof ExpressionException
                || error instanceof InterpreterException)
            return error;
        return new UserRuntimeException(error);
    }
}
