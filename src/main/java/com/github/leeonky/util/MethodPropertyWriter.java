package com.github.leeonky.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.github.leeonky.util.StringUtil.unCapitalize;

class MethodPropertyWriter<T> extends MethodProperty<T> implements PropertyWriter<T> {
    private static final int SETTER_PREFIX_LENGTH = 3;
    private String name;

    MethodPropertyWriter(BeanClass<T> beanClass, Method method) {
        super(beanClass, method);
    }

    static boolean isSetter(Method method) {
        return method.getName().startsWith("set") && method.getParameterTypes().length == 1;
    }

    @Override
    public void setValue(T bean, Object value) {
        try {
            method.invoke(bean, tryConvert(value));
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

    @Override
    public Class<?> getPropertyType() {
        return method.getParameterTypes()[0];
    }
}
