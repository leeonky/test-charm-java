package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.Converter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

public interface CurryingMethod extends ProxyObject {
    static InstanceCurryingMethod createCurryingMethod(Object instance, Method method, Converter converter,
                                                       DALRuntimeContext context) {
        if (Modifier.isStatic(method.getModifiers()))
            return new StaticCurryingMethod(instance, method, converter, context);
        return new InstanceCurryingMethod(instance, method, converter, context);
    }

    CurryingMethod call(Object arg);

    Object resolve();

    Set<Object> fetchArgRange();

    Object convertToArgType(Object obj);

    @Override
    default Object getValue(Object property) {
        return call(property).resolve();
    }

    @Override
    default Set<?> getPropertyNames() {
        return fetchArgRange();
    }
}
