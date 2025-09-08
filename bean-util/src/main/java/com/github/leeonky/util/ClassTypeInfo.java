package com.github.leeonky.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.leeonky.util.Classes.named;

public class ClassTypeInfo<T> implements TypeInfo<T> {
    protected final BeanClass<T> type;
    private final PropertyProxyFactory<T> proxyFactory;
    private final Map<String, PropertyReader<T>> readers = new LinkedHashMap<>();
    private final Map<String, PropertyWriter<T>> writers = new LinkedHashMap<>();
    private final Map<String, Property<T>> properties = new LinkedHashMap<>();
    private final Map<String, PropertyReader<T>> allReaders = new LinkedHashMap<>();
    private final Map<String, PropertyWriter<T>> allWriters = new LinkedHashMap<>();

    private static final AccessorFilter ACCESSOR_FILTER = new AccessorFilter().extend();

    public ClassTypeInfo(BeanClass<T> type, PropertyProxyFactory<T> proxyFactory) {
        this.type = type;
        this.proxyFactory = Objects.requireNonNull(proxyFactory);
        collectFields(type);
        collectGetterSetters(type);
    }

    private void collectGetterSetters(BeanClass<T> type) {
        for (Method method : named(type.getType()).getMethods()) {
            if (MethodPropertyReader.isGetter(method))
                addReaders(new MethodPropertyReader<>(type, method));
            if (MethodPropertyWriter.isSetter(method))
                addWriters(new MethodPropertyWriter<>(type, method));
        }
    }

    private void collectFields(BeanClass<T> type) {
        Map<String, Field> addedReaderFields = new HashMap<>();
        Map<String, Field> addedWriterFields = new HashMap<>();
        for (Field field : type.getType().getFields()) {
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

    private void addWriters(PropertyWriter<T> writer) {
        addAccessor(proxyFactory.proxyWriter(writer), writers, allWriters);
    }

    private void addReaders(PropertyReader<T> reader) {
        addAccessor(proxyFactory.proxyReader(reader), readers, allReaders);
    }

    private <A extends PropertyAccessor<T>> void addAccessor(A accessor, Map<String, A> accessorMap,
                                                             Map<String, A> allAccessorMap) {
        allAccessorMap.put(accessor.getName(), accessor);
        if (accessor.isBeanProperty() && ACCESSOR_FILTER.test(accessor)) {

            properties.put(accessor.getName(), proxyFactory.proxyProperty(
                    new DefaultProperty<>(accessor.getName(), accessor.getBeanType())));
            accessorMap.put(accessor.getName(), accessor);
        }
    }

    @Override
    public PropertyReader<T> getReader(String property) {
        return allReaders.computeIfAbsent(property, k -> {
            throw new NoSuchAccessorException("No available property reader for " + type.getSimpleName() + "." + property);
        });
    }

    @Override
    public PropertyWriter<T> getWriter(String property) {
        return allWriters.computeIfAbsent(property, k -> {
            throw new NoSuchAccessorException("No available property writer for " + type.getSimpleName() + "." + property);
        });
    }

    @Override
    public Map<String, PropertyReader<T>> getReaders() {
        return readers;
    }

    @Override
    public Map<String, PropertyWriter<T>> getWriters() {
        return writers;
    }

    @Override
    public Map<String, Property<T>> getProperties() {
        return properties;
    }

    @Override
    public Property<T> getProperty(String name) {
        return properties.computeIfAbsent(name, k -> {
            throw new NoSuchPropertyException(type.getSimpleName() + "." + name);
        });
    }
}
