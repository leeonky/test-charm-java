package org.testcharm.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Stream;

import static org.testcharm.util.Classes.isReflective;
import static org.testcharm.util.Classes.named;

public class ClassTypeInfo<T> extends AbstractTypeInfo<T> {

    public ClassTypeInfo(BeanClass<T> type, PropertyProxyFactory<T> proxyFactory) {
        super(type, proxyFactory);
        collectFields(type);
        collectGetterSetters(type);
    }

    private void collectGetterSetters(BeanClass<T> type) {
        getterSetterOwnerTypes(type.getType()).flatMap(t -> Arrays.stream(t.getMethods()))
                .forEach(method -> {
                    if (MethodPropertyReader.isGetter(method))
                        findAccessible(method).ifPresent(m -> addReaders(new MethodPropertyReader<>(type, m)));
                    if (MethodPropertyWriter.isSetter(method))
                        findAccessible(method).ifPresent(m -> addWriters(new MethodPropertyWriter<>(type, m)));
                });
    }

    private static Optional<Method> findAccessible(Method method) {
        if (Classes.isReflective(method.getDeclaringClass()))
            return Optional.of(method);

        ArrayDeque<Class<?>> types = new ArrayDeque<>();
        addSupers(types, method.getDeclaringClass());

        while (!types.isEmpty()) {
            Class<?> type = types.removeFirst();
            Method accessible = findAccessible(method, type);
            if (accessible != null)
                return Optional.of(accessible);
            addSupers(types, type);
        }
        return Optional.empty();
    }

    private static void addSupers(ArrayDeque<Class<?>> types, Class<?> declaringClass) {
        if (declaringClass.getSuperclass() != null)
            types.addLast(declaringClass.getSuperclass());
        for (Class<?> anInterface : declaringClass.getInterfaces()) {
            types.addLast(anInterface);
        }
    }

    private static Method findAccessible(Method target, Class<?> type) {
        if (isReflective(type))
            try {
                return type.getMethod(target.getName(), target.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return null;
            }
        return null;
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
