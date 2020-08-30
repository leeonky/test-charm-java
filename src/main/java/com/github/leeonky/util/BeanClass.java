package com.github.leeonky.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.leeonky.util.Suppressor.get;
import static java.util.Arrays.asList;

public class BeanClass<T> {
    private final static Map<Class<?>, BeanClass<?>> instanceCache = new ConcurrentHashMap<>();
    private final TypeInfo<T> typeInfo;
    private final Class<T> type;
    private final Converter converter = Converter.createDefault();

    protected BeanClass(Class<T> type) {
        this.type = Objects.requireNonNull(type);
        typeInfo = TypeInfo.create(this);
    }

    public static String getClassName(Object object) {
        return object == null ? null : object.getClass().getName();
    }

    @SuppressWarnings("unchecked")
    public static <T> BeanClass<T> create(Class<T> type) {
        return (BeanClass<T>) instanceCache.computeIfAbsent(type, BeanClass::new);
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> type, Object... args) {
        List<Constructor<?>> constructors = Stream.of(type.getConstructors())
                .filter(c -> isProperConstructor(c, args))
                .collect(Collectors.toList());
        if (constructors.size() != 1)
            throw new IllegalArgumentException(String.format("No appropriate %s constructor for params [%s]",
                    type.getName(), toString(args)));
        return get(() -> (T) constructors.get(0).newInstance(args));
    }

    private static boolean isProperConstructor(Constructor<?> constructor, Object[] parameters) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        return parameterTypes.length == parameters.length && IntStream.range(0, parameterTypes.length)
                .allMatch(i -> parameterTypes[i].isInstance(parameters[i]));
    }

    private static String toString(Object[] parameters) {
        return Stream.of(parameters)
                .map(o -> o == null ? "null" : o.getClass().getName() + ":" + o)
                .collect(Collectors.joining(", "));
    }

    @SuppressWarnings("unchecked")
    public static <E> Stream<E> arrayCollectionToStream(Object collection) {
        if (collection != null) {
            Class<?> collectionType = collection.getClass();
            if (collectionType.isArray())
                return IntStream.range(0, Array.getLength(collection)).mapToObj(i -> (E) Array.get(collection, i));
            else if (collection instanceof Iterable)
                return StreamSupport.stream(((Iterable<E>) collection).spliterator(), false);
        }
        throw new IllegalArgumentException("`" + collection + "` is not collection or array");
    }

    public static List<Object> toChainNodes(String chain) {
        return Arrays.stream(chain.split("[\\[\\].]")).filter(s -> !s.isEmpty()).map(s -> {
            try {
                return Integer.valueOf(s);
            } catch (Exception ignore) {
                return s;
            }
        }).collect(Collectors.toList());
    }

    public static BeanClass<?> create(GenericType type) {
        if (!type.hasTypeArguments())
            return create(type.getRawType());
        return GenericBeanClass.create(type);
    }

    public Converter getConverter() {
        return converter;
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
        return getPropertyReaders().computeIfAbsent(property, k -> {
            throw new IllegalArgumentException("No available property reader for " + type.getSimpleName() + "." + property);
        });
    }

    public BeanClass<T> setPropertyValue(T bean, String property, Object value) {
        getPropertyWriter(property).setValue(bean, value);
        return this;
    }

    public PropertyWriter<T> getPropertyWriter(String property) {
        return getPropertyWriters().computeIfAbsent(property, k -> {
            throw new IllegalArgumentException("No available property writer for " + type.getSimpleName() + "." + property);
        });
    }

    public T newInstance(Object... args) {
        return newInstance(type, args);
    }

    @SuppressWarnings("unchecked")
    public Object createCollection(List<?> elements) {
        if (getType().isArray()) {
            Object array = Array.newInstance(getType().getComponentType(), elements.size());
            for (int i = 0; i < elements.size(); i++)
                Array.set(array, i, elements.get(i));
            return array;
        }
        if (getType().isInterface()) {
            if (Set.class.isAssignableFrom(getType()))
                return new LinkedHashSet<>(elements);
            if (Iterable.class.isAssignableFrom(getType()))
                return new ArrayList<>(elements);
        } else {
            if (Collection.class.isAssignableFrom(getType())) {
                Collection<Object> collection = (Collection<Object>) newInstance();
                collection.addAll(elements);
                return collection;
            }
        }
        throw new IllegalStateException(String.format("Cannot create instance of collection type %s", getName()));
    }

    public Object getPropertyChainValue(T object, String chain) {
        return getPropertyChainValue(object, toChainNodes(chain));
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
        if (p instanceof Integer) {
            Object[] array = BeanClass.arrayCollectionToStream(object).toArray();
            if ((int) p >= array.length)
                throw new NullPointerInChainException(originalChain, level);
            Object element = array[(int) p];
            if (chain.isEmpty())
                return element;
            if (element == null)
                throw new NullPointerInChainException(originalChain, level + 1);
            return ((BeanClass) BeanClass.create(element.getClass())).getPropertyChainValueInner(originalChain, level + 1, element, chain);
        } else {
            PropertyReader propertyReader = getPropertyReader((String) p);
            return propertyReader.getType().getPropertyChainValueInner(originalChain, level + 1, propertyReader.getValue(object), chain);
        }
    }

    public PropertyReader<?> getPropertyChainReader(String chain) {
        return getPropertyChainReader(toChainNodes(chain));
    }

    public PropertyReader<?> getPropertyChainReader(List<Object> chain) {
        return getPropertyChainReaderInner(new LinkedList<>(chain));
    }

    public PropertyReader<?> getPropertyChainReaderInner(LinkedList<Object> chain) {
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
            return getTypeArguments(0)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Should specify generic type %s.%s", getName(), getName())));
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
        return obj.getClass().equals(BeanClass.class) && Objects.equals(((BeanClass) obj).getType(), type);
    }

    @SuppressWarnings("unchecked")
    public <S> BeanClass<S> getSuper(Class<S> target) {
        List<BeanClass> superBeanClasses = supers();
        return (BeanClass<S>) superBeanClasses.stream().filter(beanClass -> beanClass.getType().equals(target))
                .findFirst().orElseGet(() -> (BeanClass<S>) superBeanClasses.stream()
                        .map(beanClass -> beanClass.getSuper(target))
                        .filter(Objects::nonNull).findFirst().orElse(null));
    }

    private List<BeanClass> supers() {
        List<Type> suppers = new ArrayList<>(asList(type.getGenericInterfaces()));
        suppers.add(type.getGenericSuperclass());
        return suppers.stream().filter(Objects::nonNull)
                .map(t -> BeanClass.create(GenericType.createGenericType(t)))
                .collect(Collectors.toList());
    }
}
