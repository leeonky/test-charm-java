package org.testcharm.util;

import java.lang.reflect.Method;

public class InterfaceProxyTypeInfo<T> extends AbstractTypeInfo<T> {
    public InterfaceProxyTypeInfo(BeanClass<T> type, PropertyProxyFactory<T> proxyFactory) {
        super(type, proxyFactory);

        for (Method method : type.getType().getMethods()) {
            if (MethodPropertyReader.isGetter(method)) {
                MethodPropertyReader<T> reader = new MethodPropertyReader<>(type, method);
                addReaders(reader);
                addWriters(new ProxyPropertyWriter<>(type, reader));
            }
        }
    }
}
