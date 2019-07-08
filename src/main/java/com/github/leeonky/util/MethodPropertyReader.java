package com.github.leeonky.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class MethodPropertyReader<T> implements PropertyReader<T> {
    private final Method method;
    private String name;

    MethodPropertyReader(Method method) {
        this.method = method;
    }

    private static String unCapitalize(String str) {
        return str.isEmpty() ? str : str.toLowerCase().substring(0, 1) + str.substring(1);
    }

    static boolean isGetter(Method method) {
        String methodName = method.getName();
        return method.getParameters().length == 0 &&
                (method.getReturnType().equals(boolean.class) ?
                        methodName.startsWith("is") : methodName.startsWith("get"));
    }

    @Override
    public Object getValue(T bean) {
        try {
            return method.invoke(bean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getName() {
        if (name == null)
            return name = propertyName(method);
        return name;
    }

    private String propertyName(Method method) {
        String methodName = method.getName();
        return unCapitalize(method.getReturnType().equals(boolean.class) ?
                methodName.replaceFirst("^is", "") : methodName.replaceFirst("^get", ""));
    }
}
