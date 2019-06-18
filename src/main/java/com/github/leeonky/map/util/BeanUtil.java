package com.github.leeonky.map.util;

import java.lang.reflect.Method;

public class BeanUtil {
    public static Object getPropertyValue(Object instance, String name) throws Exception {
        try {
            return requireGetter(instance.getClass().getMethod("get" + StringUtil.capitalize(name))).invoke(instance);
        } catch (Exception ex) {
            try {
                return requireGetter(instance.getClass().getMethod("is" + StringUtil.capitalize(name))).invoke(instance);
            } catch (Exception e) {
                return instance.getClass().getField(name).get(instance);
            }
        }
    }

    private static Method requireGetter(Method method) {
        return isGetter(method) ? method : null;
    }

    private static boolean isGetter(Method m) {
        if (m.getParameters().length == 0) {
            if (m.getName().startsWith("get") && !m.getReturnType().equals(void.class) && !m.getName().equals("getClass"))
                return true;
            return m.getName().startsWith("is") && m.getReturnType().equals(boolean.class);
        }
        return false;
    }
}
