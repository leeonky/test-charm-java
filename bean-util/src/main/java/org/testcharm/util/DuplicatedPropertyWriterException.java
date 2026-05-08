package org.testcharm.util;

import java.lang.reflect.Method;

public class DuplicatedPropertyWriterException extends RuntimeException {
    private final Method exist;
    private final Method method;

    public DuplicatedPropertyWriterException(Method exist, Method method) {
        this.exist = exist;
        this.method = method;
    }

    @Override
    public String getMessage() {
        IndentBuffer indentBuffer = IndentBuffer.create();
        indentBuffer.append("Duplicated setter:").indent().newLine().append(exist).newLine().append(method);
        return indentBuffer.toString();
    }
}
