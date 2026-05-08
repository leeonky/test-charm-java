package org.testcharm.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;

import static org.testcharm.util.MethodPropertyReader.propertyName;
import static org.testcharm.util.Sneaky.execute;

class MethodPropertyWriter<T> extends MethodPropertyAccessor<T> implements PropertyWriter<T> {
    private static final int SETTER_PREFIX_LENGTH = 3;
    private final BiConsumer<T, Object> SETTER = (bean, value) -> execute(() -> getMethod().invoke(bean, value));
    private String name;

    MethodPropertyWriter(BeanClass<T> beanClass, Method method) {
        super(beanClass, method);
    }

    static boolean isSetter(Method method) {
        return method.getName().startsWith("set") && method.getName().length() > 3 && method.getParameterTypes().length == 1;
    }

    @Override
    public BiConsumer<T, Object> setter() {
        return SETTER;
    }

    @Override
    public String getName() {
        if (name == null)
            return name = propertyName(getMethod().getName().substring(SETTER_PREFIX_LENGTH));
        return name;
    }

    @Override
    public Type getGenericType() {
        return getMethod().getGenericParameterTypes()[0];
    }
}
