package org.testcharm.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AbstractTypeInfo<T> implements TypeInfo<T> {
    protected static final AccessorFilter ACCESSOR_FILTER = new AccessorFilter().extend();
    protected final BeanClass<T> type;
    protected final PropertyProxyFactory<T> proxyFactory;
    protected final Map<String, PropertyReader<T>> readers = new LinkedHashMap<>();
    protected final Map<String, PropertyWriter<T>> writers = new LinkedHashMap<>();
    protected final Map<String, Property<T>> properties = new LinkedHashMap<>();
    protected final Map<String, PropertyReader<T>> allReaders = new LinkedHashMap<>();
    protected final Map<String, PropertyWriter<T>> allWriters = new LinkedHashMap<>();

    public AbstractTypeInfo(BeanClass<T> type, PropertyProxyFactory<T> proxyFactory) {
        this.type = type;
        this.proxyFactory = Objects.requireNonNull(proxyFactory);
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

    protected void addWriters(PropertyWriter<T> writer) {
        addAccessor(proxyFactory.proxyWriter(writer), writers, allWriters);
    }

    protected void addReaders(PropertyReader<T> reader) {
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
}
