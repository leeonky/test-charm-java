package com.github.leeonky.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.github.leeonky.util.StringUtil.unCapitalize;

class MethodPropertyReader<T> extends MethodProperty<T> implements PropertyReader<T> {
    private static final int BOOLEAN_GETTER_PREFIX_LENGTH = 2;
    private static final int GETTER_PREFIX_LENGTH = 3;
    private String name;

    MethodPropertyReader(BeanClass<T> beanClass, Method method) {
        super(beanClass, method);
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
    protected Type provideGenericType() {
        return method.getGenericReturnType();
    }

    @Override
    public String getName() {
        if (name == null) {
            String methodName = method.getName();
            return name = unCapitalize(method.getReturnType().equals(boolean.class) ?
                    methodName.substring(BOOLEAN_GETTER_PREFIX_LENGTH) : methodName.substring(GETTER_PREFIX_LENGTH));
        }
        return name;
    }

    @Override
    public Class<?> getPropertyType() {
        return method.getReturnType();
    }
}
