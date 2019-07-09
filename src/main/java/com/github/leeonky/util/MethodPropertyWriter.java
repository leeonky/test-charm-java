package com.github.leeonky.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.github.leeonky.util.StringUtil.unCapitalize;

class MethodPropertyWriter<T> implements PropertyWriter<T> {
    public static final int SETTER_PREFIX_LENGTH = 3;
    private final Method method;
    private String name;

    MethodPropertyWriter(Method method) {
        this.method = method;
    }

    @Override
    public void setValue(T bean, Object value) {
        try {
            method.invoke(bean, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getName() {
        if (name == null)
            return name = unCapitalize(method.getName().substring(SETTER_PREFIX_LENGTH));
        return name;
    }
}
