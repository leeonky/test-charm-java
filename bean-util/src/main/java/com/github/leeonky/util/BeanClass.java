package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class BeanClass<T> {
    private final static Map<Class<?>, BeanClass<?>> instanceCache = new ConcurrentHashMap<>();
    private static Converter converter = Converter.getInstance();
    private final TypeInfo<T> typeInfo;
    private final Class<T> type;

    @SuppressWarnings("unchecked")
    protected BeanClass(Class<T> type) {
        this.type = Objects.requireNonNull(type);
        typeInfo = TypeInfo.create(this, PropertyProxyFactory.NO_PROXY);
    }

    public BeanClass(Class<T> type, PropertyProxyFactory<T> proxyFactory) {
        this.type = Objects.requireNonNull(type);
        typeInfo = TypeInfo.create(this, proxyFactory);
    }

    @SuppressWarnings("unchecked")
    public static <T> BeanClass<T> create(Class<T> type) {
        return (BeanClass<T>) instanceCache.computeIfAbsent(type, BeanClass::new);
    }

    public static <T> Optional<T> cast(Object value, Class<T> type) {
        return ofNullable(value)
                .filter(type::isInstance)
                .map(type::cast);
    }

    public static BeanClass<?> create(GenericType type) {
        if (!type.hasTypeArguments())
            return create(type.getRawType());
        return GenericBeanClass.create(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(T instance) {
        return (Class<T>) Objects.requireNonNull(instance).getClass();
    }

    public static <T> BeanClass<T> createFrom(T instance) {
        return create(getClass(instance));
    }

    public static Converter getConverter() {
        return converter;
    }

    public static void setConverter(Converter converter) {
        BeanClass.converter = converter;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return type.getName();
    }

    public String getSimpleName() {
        return type.getSimpleName();
    }

    public Map<String, PropertyReader<T>> getPropertyReaders() {
        return typeInfo.getReaders();
    }

    public Map<String, PropertyWriter<T>> getPropertyWriters() {
        return typeInfo.getWriters();
    }

    public Object getPropertyValue(T bean, String property) {
        return getPropertyReader(property).getValue(bean);
    }

    public PropertyReader<T> getPropertyReader(String property) {
        return typeInfo.getReader(property);
    }

    public BeanClass<T> setPropertyValue(T bean, String property, Object value) {
        getPropertyWriter(property).setValue(bean, value);
        return this;
    }

    public PropertyWriter<T> getPropertyWriter(String property) {
        return typeInfo.getWriter(property);
    }

    public T newInstance(Object... args) {
        return Classes.newInstance(type, args);
    }

    public Object createCollection(Collection<?> elements) {
        return CollectionHelper.createCollection(elements, this);
    }

    public Object getPropertyChainValue(T object, String chain) {
        return getPropertyChainValue(object, Property.toChainNodes(chain));
    }

    public Object getPropertyChainValue(T object, List<Object> chain) {
        return getPropertyChainValueInner(chain, 0, object, new LinkedList<>(chain));
    }

    @SuppressWarnings("unchecked")
    private Object getPropertyChainValueInner(List<Object> originalChain, int level, T object, LinkedList<Object> chain) {
        if (chain.isEmpty())
            return object;
        if (object == null)
            throw new NullPointerInChainException(originalChain, level);
        Object p = chain.removeFirst();
        PropertyReader propertyReader = getPropertyReader(p.toString());
        return propertyReader.getType().getPropertyChainValueInner(originalChain, level + 1,
                propertyReader.getValue(object), chain);
    }

    public PropertyReader<?> getPropertyChainReader(String chain) {
        return getPropertyChainReader(Property.toChainNodes(chain));
    }

    public PropertyReader<?> getPropertyChainReader(List<Object> chain) {
        return getPropertyChainReaderInner(new LinkedList<>(chain));
    }

    private PropertyReader<?> getPropertyChainReaderInner(LinkedList<Object> chain) {
        return getPropertyReader((String) chain.removeFirst()).getPropertyChainReader(chain);
    }

    @SuppressWarnings("unchecked")
    public T createDefault() {
        return (T) Array.get(Array.newInstance(getType(), 1), 0);
    }

    public boolean hasTypeArguments() {
        return false;
    }

    public Optional<BeanClass<?>> getTypeArguments(int position) {
        return Optional.empty();
    }

    public BeanClass<?> getElementType() {
        if (type.isArray())
            return BeanClass.create(type.getComponentType());
        if (Iterable.class.isAssignableFrom(type))
            return getTypeArguments(0).orElseGet(() -> BeanClass.create(Object.class));
        return null;
    }

    public BeanClass<?> getElementOrPropertyType() {
        BeanClass<?> elementType = getElementType();
        return elementType == null ? this : elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(BeanClass.class, type);
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(BeanClass.class) && Objects.equals(((BeanClass<?>) obj).getType(), type);
    }

    @SuppressWarnings("unchecked")
    public <S> BeanClass<S> getSuper(Class<S> target) {
        List<BeanClass<?>> superBeanClasses = supers();
        return (BeanClass<S>) superBeanClasses.stream().filter(beanClass -> beanClass.getType().equals(target))
                .findFirst().orElseGet(() -> superBeanClasses.stream()
                        .map(beanClass -> beanClass.getSuper(target))
                        .filter(Objects::nonNull).findFirst().orElse(null));
    }

    private List<BeanClass<?>> supers() {
        List<Type> suppers = new ArrayList<>(asList(type.getGenericInterfaces()));
        suppers.add(type.getGenericSuperclass());
        return suppers.stream().filter(Objects::nonNull)
                .map(t -> BeanClass.create(GenericType.createGenericType(t)))
                .collect(toList());
    }

    public boolean isCollection() {
        return getType().isArray() || Iterable.class.isAssignableFrom(getType());
    }

    public Map<String, Property<T>> getProperties() {
        return typeInfo.getProperties();
    }

    public Property<T> getProperty(String name) {
        return typeInfo.getProperty(name);
    }

    public boolean isInstance(Object instance) {
        return type.isInstance(instance);
    }

    public boolean isInheritedFrom(Class<?> type) {
        return type.isAssignableFrom(this.type);
    }

    public <A extends Annotation> Optional<A> annotation(Class<A> annotationClass) {
        return ofNullable(getAnnotation(annotationClass));
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return type.getAnnotation(annotationClass);
    }

    public Type getGenericType() {
        return getType();
    }
}
