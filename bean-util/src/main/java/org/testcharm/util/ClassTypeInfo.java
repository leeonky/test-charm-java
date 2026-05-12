package org.testcharm.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.testcharm.util.Classes.named;

public class ClassTypeInfo<T> extends AbstractTypeInfo<T> {

    public ClassTypeInfo(BeanClass<T> type, PropertyProxyFactory<T> proxyFactory) {
        super(type, proxyFactory);
        collectFields(type);
        collectGetterSetters(type);
    }

    private void collectGetterSetters(BeanClass<T> type) {
        getterSetterOwnerTypes(type.getType()).flatMap(t -> Arrays.stream(t.getMethods()))
                .filter(method -> isValidDeclaringClass(method.getDeclaringClass()))
                .forEach(method -> {
                    if (MethodPropertyReader.isGetter(method))
                        addReaders(new MethodPropertyReader<>(type, method));
                    if (MethodPropertyWriter.isSetter(method))
                        addWriters(new MethodPropertyWriter<>(type, method));
                });
    }

    private Stream<Class<?>> getterSetterOwnerTypes(Class<?> type) {
        if (Proxy.isProxyClass(type))
            return Arrays.stream(type.getInterfaces());
        return Stream.of(named(type));
    }

    private void collectFields(BeanClass<T> type) {
        Map<String, Field> addedReaderFields = new HashMap<>();
        Map<String, Field> addedWriterFields = new HashMap<>();
        for (Field field : type.getType().getFields()) {
            if (isValidDeclaringClass(field.getDeclaringClass())) {
                Field addedReaderField = addedReaderFields.get(field.getName());
                if (addedReaderField == null || addedReaderField.getType().equals(type.getType())) {
                    addReaders(new FieldPropertyReader<>(type, field));
                    addedReaderFields.put(field.getName(), field);
                }
                if (!Modifier.isFinal(field.getModifiers())) {
                    Field addedWriterField = addedWriterFields.get(field.getName());
                    if (addedWriterField == null || addedWriterField.getType().equals(type.getType())) {
                        addWriters(new FieldPropertyWriter<>(type, field));
                        addedWriterFields.put(field.getName(), field);
                    }
                }
            }
        }
    }

    private boolean isValidDeclaringClass(Class<?> declaringClass) {
        return !declaringClass.isAnonymousClass() && !declaringClass.isLocalClass();
    }
}
