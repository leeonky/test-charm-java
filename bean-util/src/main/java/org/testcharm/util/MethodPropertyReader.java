package org.testcharm.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static org.testcharm.util.Sneaky.execute;
import static org.testcharm.util.StringUtil.unCapitalize;

class MethodPropertyReader<T> extends MethodPropertyAccessor<T> implements PropertyReader<T> {
    private static final int BOOLEAN_GETTER_PREFIX_LENGTH = 2;
    private static final int GETTER_PREFIX_LENGTH = 3;
    private String name;

    MethodPropertyReader(BeanClass<T> beanClass, Method method) {
        super(beanClass, method);
    }

    static boolean isGetter(Method method) {
        String methodName = method.getName();
        return method.getParameters().length == 0 && (!method.getReturnType().equals(void.class))
                && (method.getReturnType().equals(boolean.class) ?
                methodName.startsWith("is") : (methodName.startsWith("get") && methodName.length() > 3));
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
            return name = passReaderName(getMethod());
        }
        return name;
    }

    static String passReaderName(Method method) {
        String methodName = method.getName();
        return propertyName(method.getReturnType().equals(boolean.class) ?
                methodName.substring(BOOLEAN_GETTER_PREFIX_LENGTH) : methodName.substring(GETTER_PREFIX_LENGTH));
    }

    static String propertyName(String name) {
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1)))
            return name;
        return unCapitalize(name);
    }

    @Override
    public boolean isBeanProperty() {
        return super.isBeanProperty() && !getMethod().getName().equals("getClass");
    }
}
