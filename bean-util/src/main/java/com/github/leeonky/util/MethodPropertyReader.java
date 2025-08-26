package com.github.leeonky.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.github.leeonky.util.Sneaky.execute;
import static com.github.leeonky.util.StringUtil.unCapitalize;

class MethodPropertyReader<T> extends MethodPropertyAccessor<T> implements PropertyReader<T> {
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
                        methodName.startsWith("is") : (methodName.startsWith("get")));
    }

    @Override
    public Object getValue(T instance) {
        return execute(() -> getMethod().invoke(instance));
    }

    @Override
    public Type getGenericType() {
        return getMethod().getGenericReturnType();
    }

    @Override
    public String getName() {
        if (name == null) {
            String methodName = getMethod().getName();
            return name = unCapitalize(getMethod().getReturnType().equals(boolean.class) ?
                    methodName.substring(BOOLEAN_GETTER_PREFIX_LENGTH) : methodName.substring(GETTER_PREFIX_LENGTH));
        }
        return name;
    }

    @Override
    public boolean isBeanProperty() {
        return super.isBeanProperty() && !getMethod().getName().equals("getClass");
    }
}
