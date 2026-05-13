package org.testcharm.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class InterfaceProxyTypeInfo<T> extends AbstractTypeInfo<T> {
    public InterfaceProxyTypeInfo(BeanClass<T> type, PropertyProxyFactory<T> proxyFactory) {
        super(type, proxyFactory);

        Method[] methods = type.getType().getMethods();
        Map<String, MethodPropertyWriter<T>> setters = new HashMap<>();
        for (Method method : methods) {
            if (MethodPropertyWriter.isSetter(method)) {
                MethodPropertyWriter<T> originalSetter = new MethodPropertyWriter<>(type, method);
                setters.put(originalSetter.getName(), originalSetter);
            }
        }

        for (Method method : methods) {
            if (MethodPropertyReader.isGetter(method)) {
                MethodPropertyReader<T> reader = new MethodPropertyReader<>(type, method);
                addReaders(reader);
                addWriters(new ProxyPropertyWriter<>(type, reader, setters.get(reader.getName())));
            }
        }
    }
}
