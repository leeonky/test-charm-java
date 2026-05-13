package org.testcharm.util;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanClassProxy {
    public interface PropertyHolder {
        Map<String, Object> __properties();
    }

    private final static Map<Class<?>, BeanClass<?>> instanceCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> BeanClass<T> create(Class<T> type) {
        BeanClass<T> beanClass = BeanClass.create(type);
        if (!beanClass.isCollection() && type.isInterface())
            return (BeanClass<T>) instanceCache.computeIfAbsent(type, t -> new BeanClass<T>((Class<T>) t,
                    instance -> new InterfaceProxyTypeInfo<T>(instance, PropertyProxyFactory.NO_PROXY)) {
                @Override
                public T newInstance(Object... args) {
                    Map<String, Object> properties = new HashMap<>();
                    return (T) Proxy.newProxyInstance(getType().getClassLoader(),
                            new Class[]{getType(), PropertyHolder.class},
                            (o, method, objects) -> {
                                if (MethodPropertyReader.isGetter(method)) {
                                    String propertyName = MethodPropertyReader.passReaderName(method);
                                    PropertyReader<T> propertyReader = getPropertyReader(propertyName);
                                    return properties.getOrDefault(propertyName, propertyReader.getType().createDefault());
                                }
                                switch (method.getName()) {
                                    case "__properties":
                                        return properties;
                                    case "equals":
                                        return o == objects[0];
                                    case "toString":
                                        return "Proxy[" + getType().getName() + "]" + properties;
                                    case "hashCode":
                                        return System.identityHashCode(o);
                                }
                                return null;
                            }
                    );
                }
            });
        return beanClass;
    }
}
